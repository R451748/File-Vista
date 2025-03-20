package com.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.file.entity.FileEntity;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    // ✅ Find a file by its name (to prevent duplicate file creation)
    Optional<FileEntity> findByFileName(String fileName);
}
