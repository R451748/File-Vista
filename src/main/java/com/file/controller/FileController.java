package com.file.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.file.entity.FileEntity;
import com.file.service.FileService;

import jakarta.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    // List all files
    @GetMapping
    public String listFiles(Model model) {
        List<FileEntity> files = fileService.getAllFiles();
        model.addAttribute("files", files);
        return "index"; // Show the files in the index page
    }

    // Edit file (GET request for the file to edit)
    @GetMapping("/edit/{id}")
    public String editFile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            FileEntity fileEntity = fileService.getFileById(id);
            model.addAttribute("fileId", id);
            model.addAttribute("fileName", fileEntity.getFileName());

            // Check if fileType is not null and the file is a text type
            if (fileEntity.getFileType() != null && fileEntity.getFileType().startsWith("text")) {
                model.addAttribute("fileContent", new String(fileEntity.getData())); // Display as text for editing
            } else {
                model.addAttribute("fileContent", ""); // Handle binary files or other types (no content displayed)
                model.addAttribute("errorMessage", "Cannot edit non-text files."); // Inform the user
            }

            return "edit"; // Return edit page to update the file content
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching file: " + e.getMessage());
            return "redirect:/files";
        }
    }

    // Handle file updates (POST request after editing)
    @PostMapping("/update/{id}")
    public String updateFile(@PathVariable Long id, @RequestParam("fileContent") String fileContent, RedirectAttributes redirectAttributes) {
        try {
            FileEntity existingFile = fileService.getFileById(id);

            // If it's a text file, update the content
            if (existingFile.getFileType().startsWith("text")) {
                existingFile.setData(fileContent.getBytes()); // Update content as bytes
            } else {
                throw new RuntimeException("Cannot edit non-text files.");
            }

            fileService.save(existingFile); // Save the updated file
            redirectAttributes.addFlashAttribute("successMessage", "File updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating file: " + e.getMessage());
        }
        return "redirect:/files"; // Redirect back to the list of files
    }

    // Handle file upload
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            fileService.uploadFile(file);
            redirectAttributes.addFlashAttribute("successMessage", "File uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading file: " + e.getMessage());
        }
        return "redirect:/files";
    }

    // Delete file
    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fileService.deleteFile(id); // Delete the file
            redirectAttributes.addFlashAttribute("successMessage", "File deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting file: " + e.getMessage());
        }
        return "redirect:/files"; // Redirect back to the list of files
    }

    // Ensure you have a proper controller method for creating a directory
    @PostMapping("/create-directory")
    public String createDirectory(@RequestParam("directoryName") String directoryName, RedirectAttributes redirectAttributes) {
        try {
            fileService.createDirectory(directoryName); // Assume you have a method to handle this
            redirectAttributes.addFlashAttribute("successMessage", "Directory created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating directory: " + e.getMessage());
        }
        return "redirect:/files"; // Redirect to the files page
    }
    @PostMapping("/create-file")
    public String createFile(@RequestParam("fileName") String fileName, Model model) {
        try {
            // Create a new file (or handle the file creation logic)
            fileService.createFile(fileName);
            model.addAttribute("successMessage", "File created successfully!");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error creating file: " + e.getMessage());
        }
        return "file-manager";  // Return the name of the view to render
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

        if (fileEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FileEntity fileEntity = fileEntityOptional.get();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                .body(new ByteArrayResource(fileEntity.getData()));
    }


}
