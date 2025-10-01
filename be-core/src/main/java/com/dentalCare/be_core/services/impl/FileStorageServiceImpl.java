package com.dentalCare.be_core.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dentalCare.be_core.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_FILE_TYPES =
            Arrays.asList("image/jpeg", "image/jpg", "image/png", "application/pdf");

    public FileStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
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
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "dental-care/patient_" + patientId + "/entry_" + entryId,
                            "public_id", System.currentTimeMillis() + "_" + file.getOriginalFilename(),
                            "resource_type", "auto" // ✅ permite imágenes, pdf, videos
                    )
            );

            return uploadResult.get("secure_url").toString(); // ✅ URL de Cloudinary
        } catch (IOException ex) {
            throw new RuntimeException("Could not upload file to Cloudinary", ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "auto"));
            }
        } catch (Exception e) {
            log.error("Could not delete file from Cloudinary: {}", fileUrl, e);
        }
    }

    private String extractPublicId(String url) {
        if (url == null) return null;

        // ejemplo: https://res.cloudinary.com/demo/image/upload/v123456789/folder/file.png
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1]; // file.png
        String folderPath = parts[parts.length - 2]; // folder
        return folderPath + "/" + fileName.split("\\.")[0];
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
