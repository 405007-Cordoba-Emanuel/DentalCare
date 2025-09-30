package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation = Paths.get("uploads/medical-history").toAbsolutePath().normalize();
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "application/pdf");

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long patientId, Long entryId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!isValidFileType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only JPG, PNG and PDF are allowed");
        }

        if (!isValidFileSize(file.getSize())) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                throw new IllegalArgumentException("File name is invalid");
            }
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = patientId + "_" + entryId + "_" + System.currentTimeMillis() + fileExtension;

            Path patientDir = this.fileStorageLocation.resolve(patientId.toString());
            Files.createDirectories(patientDir);

            Path targetLocation = patientDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return patientId + "/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }


    @Override
    public void deleteFile(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.error("Could not delete file " + filePath, ex);
        }
    }

    @Override
    public boolean isValidFileType(String fileType) {
        return ALLOWED_FILE_TYPES.contains(fileType);
    }

    @Override
    public boolean isValidFileSize(long fileSize) {
        return fileSize <= MAX_FILE_SIZE;
    }
}
