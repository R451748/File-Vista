package com.file.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.file.entity.FileEntity;
import com.file.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    
    private static final String UPLOAD_DIR = "C:/uploaded_files/";

    @Autowired
    private FileRepository fileRepository;

    // ✅ Upload file method (save both to disk and DB)
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Define file path
        String directory = "C:/uploaded_files/";
        File saveFile = new File(directory, file.getOriginalFilename());

        // Ensure directory exists
        File dir = new File(directory);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create upload directory");
        }

        // ✅ Save file properly
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // ✅ Save to database
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setData(file.getBytes());  // Store file content
        fileEntity.setDirectory(false);
        fileRepository.save(fileEntity);

        return saveFile.getAbsolutePath(); // Return saved file path
    }



    // ✅ Retrieve all files
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    // ✅ Get file by ID
    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));
    }

    // ✅ Download file
    public Optional<FileEntity> getFile(Long id) {
        return fileRepository.findById(id);
    }


    // ✅ Delete file
    public void deleteFile(Long id) throws IOException {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(id);
        if (fileEntityOptional.isPresent()) {
            FileEntity fileEntity = fileEntityOptional.get();
            Path filePath = Paths.get(UPLOAD_DIR, fileEntity.getFileName());
            Files.deleteIfExists(filePath);
            fileRepository.deleteById(id);
        } else {
            throw new IOException("File not found with ID: " + id);
        }
    }

    // ✅ Create directory
    public void createDirectory(String directoryName) throws IOException {
        Path directoryPath = Paths.get(UPLOAD_DIR, directoryName);
        if (Files.notExists(directoryPath)) {
            Files.createDirectories(directoryPath);
            FileEntity directoryEntity = new FileEntity();
            directoryEntity.setFileName(directoryName);
            directoryEntity.setDirectory(true);
            fileRepository.save(directoryEntity);
        } else {
            throw new IOException("Directory already exists: " + directoryName);
        }
    }
    public void save(FileEntity fileEntity) {
        fileRepository.save(fileEntity);
    }
    public void createFile(String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IOException("Invalid file name");
        }

        // Full file path
        String filePath = UPLOAD_DIR + fileName;

        // Check if file already exists in DB
        Optional<FileEntity> existingFile = fileRepository.findByFileName(fileName);
        if (existingFile.isPresent()) {
            throw new IOException("File already exists: " + fileName);
        }

        // Create the file on disk
        File newFile = new File(filePath);
        if (newFile.createNewFile()) {
            // Save file details in DB
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setFileType(Files.probeContentType(Paths.get(filePath)));
            fileEntity.setData(new byte[0]); // Empty file
            fileEntity.setDirectory(false);

            fileRepository.save(fileEntity);
        } else {
            throw new IOException("Failed to create file: " + fileName);
        }
    }
}