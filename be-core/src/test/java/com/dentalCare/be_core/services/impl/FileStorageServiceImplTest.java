package com.dentalCare.be_core.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {

	@Mock
	private Cloudinary cloudinary;

	@Mock
	private Uploader uploader;

	@Mock
	private MultipartFile file;

	@InjectMocks
	private FileStorageServiceImpl fileStorageService;

	private Map<String, Object> uploadResult;

	@BeforeEach
	void setUp() {
		uploadResult = new HashMap<>();
		uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/v123/dental-care/patient_1/entry_1/file.jpg");
		uploadResult.put("public_id", "dental-care/patient_1/entry_1/file");
	}

	// ==================== STORE FILE ====================

	@Test
	void storeFile_Success() throws IOException {
		// Arrange
		Long patientId = 1L;
		Long entryId = 1L;
		byte[] fileBytes = "test content".getBytes();

		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("image/jpeg");
		when(file.getSize()).thenReturn(1024L);
		when(file.getBytes()).thenReturn(fileBytes);
		when(file.getOriginalFilename()).thenReturn("test.jpg");
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

		// Act
		String result = fileStorageService.storeFile(file, patientId, entryId);

		// Assert
		assertNotNull(result);
		assertTrue(result.contains("cloudinary.com"));
		verify(cloudinary.uploader()).upload(eq(fileBytes), anyMap());
	}

	@Test
	void storeFile_EmptyFile() {
		// Arrange
		when(file.isEmpty()).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> fileStorageService.storeFile(file, 1L, 1L));

		assertTrue(exception.getMessage().contains("File is empty"));
	}

	@Test
	void storeFile_FileSizeExceedsLimit() {
		// Arrange
		long fileSizeOver5MB = 6 * 1024 * 1024; // 6 MB
		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("image/jpeg");
		when(file.getSize()).thenReturn(fileSizeOver5MB);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> fileStorageService.storeFile(file, 1L, 1L));

		assertTrue(exception.getMessage().contains("File size exceeds maximum limit"));
	}

	@Test
	void storeFile_PdfFile() throws IOException {
		// Arrange
		Long patientId = 2L;
		Long entryId = 2L;
		byte[] fileBytes = "pdf content".getBytes();

		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getSize()).thenReturn(2048L);
		when(file.getBytes()).thenReturn(fileBytes);
		when(file.getOriginalFilename()).thenReturn("document.pdf");
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

		// Act
		String result = fileStorageService.storeFile(file, patientId, entryId);

		// Assert
		assertNotNull(result);
		verify(cloudinary.uploader()).upload(eq(fileBytes), anyMap());
	}

	@Test
	void storeFile_PngFile() throws IOException {
		// Arrange
		byte[] fileBytes = "png content".getBytes();

		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("image/png");
		when(file.getSize()).thenReturn(2048L);
		when(file.getBytes()).thenReturn(fileBytes);
		when(file.getOriginalFilename()).thenReturn("image.png");
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

		// Act
		String result = fileStorageService.storeFile(file, 1L, 1L);

		// Assert
		assertNotNull(result);
	}

	@Test
	void storeFile_JpgFile() throws IOException {
		// Arrange
		byte[] fileBytes = "jpg content".getBytes();

		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("image/jpg");
		when(file.getSize()).thenReturn(2048L);
		when(file.getBytes()).thenReturn(fileBytes);
		when(file.getOriginalFilename()).thenReturn("image.jpg");
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

		// Act
		String result = fileStorageService.storeFile(file, 1L, 1L);

		// Assert
		assertNotNull(result);
	}

	@Test
	void storeFile_CloudinaryThrowsIOException() throws IOException {
		// Arrange
		byte[] fileBytes = "test content".getBytes();

		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("image/jpeg");
		when(file.getSize()).thenReturn(1024L);
		when(file.getBytes()).thenReturn(fileBytes);
		when(file.getOriginalFilename()).thenReturn("test.jpg");
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> fileStorageService.storeFile(file, 1L, 1L));

		assertTrue(exception.getMessage().contains("Could not upload file to Cloudinary"));
	}

	@Test
	void storeFile_MaximumAllowedSize() throws IOException {
		// Arrange
		long exactlyFiveMB = 5 * 1024 * 1024;
		byte[] fileBytes = "test content".getBytes();

		when(file.isEmpty()).thenReturn(false);
		when(file.getContentType()).thenReturn("image/jpeg");
		when(file.getSize()).thenReturn(exactlyFiveMB);
		when(file.getBytes()).thenReturn(fileBytes);
		when(file.getOriginalFilename()).thenReturn("test.jpg");
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

		// Act
		String result = fileStorageService.storeFile(file, 1L, 1L);

		// Assert
		assertNotNull(result);
	}

	// ==================== DELETE FILE ====================

	@Test
	void deleteFile_Success() throws Exception {
		// Arrange
		String fileUrl = "https://res.cloudinary.com/demo/image/upload/v123/folder/file.jpg";
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.destroy(anyString(), anyMap())).thenReturn(new HashMap<>());

		// Act
		fileStorageService.deleteFile(fileUrl);

		// Assert
		verify(cloudinary.uploader()).destroy(anyString(), anyMap());
	}

	@Test
	void deleteFile_NullUrl() {
		// Act
		fileStorageService.deleteFile(null);

		// Assert
		verify(cloudinary, never()).uploader();
	}

	@Test
	void deleteFile_CloudinaryThrowsException() throws Exception {
		// Arrange
		String fileUrl = "https://res.cloudinary.com/demo/image/upload/v123/folder/file.jpg";
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete failed"));

		// Act - Should not throw exception, just log error
		assertDoesNotThrow(() -> fileStorageService.deleteFile(fileUrl));

		// Assert
		verify(cloudinary.uploader()).destroy(anyString(), anyMap());
	}

	@Test
	void deleteFile_ComplexUrl() throws Exception {
		// Arrange
		String fileUrl = "https://res.cloudinary.com/demo/image/upload/v123456789/dental-care/patient_5/entry_10/12345_document.pdf";
		when(cloudinary.uploader()).thenReturn(uploader);
		when(uploader.destroy(anyString(), anyMap())).thenReturn(new HashMap<>());

		// Act
		fileStorageService.deleteFile(fileUrl);

		// Assert
		verify(cloudinary.uploader()).destroy(anyString(), anyMap());
	}

	// ==================== VALIDATION METHODS ====================

	@Test
	void isValidFileType_Jpeg() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileType("image/jpeg"));
	}

	@Test
	void isValidFileType_Jpg() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileType("image/jpg"));
	}

	@Test
	void isValidFileType_Png() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileType("image/png"));
	}

	@Test
	void isValidFileType_Pdf() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileType("application/pdf"));
	}

	@Test
	void isValidFileType_InvalidType() {
		// Act & Assert
		assertFalse(fileStorageService.isValidFileType("application/exe"));
		assertFalse(fileStorageService.isValidFileType("text/plain"));
		assertFalse(fileStorageService.isValidFileType("video/mp4"));
	}

	@Test
	void isValidFileType_Null() {
		// Act & Assert
		assertFalse(fileStorageService.isValidFileType(null));
	}

	@Test
	void isValidFileSize_ValidSize() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileSize(1024L)); // 1 KB
		assertTrue(fileStorageService.isValidFileSize(1024 * 1024L)); // 1 MB
		assertTrue(fileStorageService.isValidFileSize(5 * 1024 * 1024L)); // 5 MB exactly
	}

	@Test
	void isValidFileSize_InvalidSize() {
		// Act & Assert
		assertFalse(fileStorageService.isValidFileSize(6 * 1024 * 1024L)); // 6 MB
		assertFalse(fileStorageService.isValidFileSize(10 * 1024 * 1024L)); // 10 MB
	}

	@Test
	void isValidFileSize_ZeroSize() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileSize(0L));
	}

	@Test
	void isValidFileSize_NegativeSize() {
		// Act & Assert
		assertTrue(fileStorageService.isValidFileSize(-1L)); // Technically passes but should be handled at file.isEmpty()
	}
}