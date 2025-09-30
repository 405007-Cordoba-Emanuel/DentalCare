package com.dentalCare.be_core.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, Long patientId, Long entryId);

    void deleteFile(String filePath);

    boolean isValidFileType(String fileType);

    boolean isValidFileSize(long fileSize);
}
