package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.entities.*;
import com.dentalCare.be_core.repositories.*;
import com.dentalCare.be_core.services.FileStorageService;
import com.dentalCare.be_core.services.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryServiceImplTest {

	@Mock
	private MedicalHistoryRepository medicalHistoryRepository;

	@Mock
	private DentistRepository dentistRepository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private PrescriptionRepository prescriptionRepository;

	@Mock
	private TreatmentRepository treatmentRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private FileStorageService fileStorageService;

	@Mock
	private MultipartFile file;

	@InjectMocks
	private MedicalHistoryServiceImpl medicalHistoryService;

	private Dentist dentist;
	private Patient patient;
	private MedicalHistory medicalHistory;
	private MedicalHistoryRequestDto requestDto;
	private Prescription prescription;
	private Treatment treatment;
	private UserDetailDto dentistUser;
	private UserDetailDto patientUser;

	@BeforeEach
	void setUp() {
		dentist = new Dentist();
		dentist.setId(1L);
		dentist.setUserId(10L);
		dentist.setLicenseNumber("LIC123");

		patient = new Patient();
		patient.setId(2L);
		patient.setUserId(20L);
		patient.setDni("12345678A");
		patient.setDentist(dentist);
		patient.setActive(true);

		prescription = new Prescription();
		prescription.setId(1L);
		prescription.setPrescriptionDate(LocalDate.now());

		treatment = new Treatment();
		treatment.setId(1L);
		treatment.setName("Root Canal");
		treatment.setStatus(TreatmentStatus.EN_CURSO);
		treatment.setCompletedSessions(2);

		medicalHistory = new MedicalHistory();
		medicalHistory.setId(1L);
		medicalHistory.setPatient(patient);
		medicalHistory.setDentist(dentist);
		medicalHistory.setEntryDate(LocalDate.now());
		medicalHistory.setDescription("Regular checkup");
		medicalHistory.setActive(true);
		medicalHistory.setPrescription(prescription);
		medicalHistory.setTreatment(treatment);

		requestDto = new MedicalHistoryRequestDto();
		requestDto.setPatientId(2L);
		requestDto.setEntryDate(LocalDate.now());
		requestDto.setDescription("Regular checkup");
		requestDto.setPrescriptionId(1L);
		requestDto.setTreatmentId(1L);

		dentistUser = createUserDetailDto(10L, "Dr. John", "Smith");
		patientUser = createUserDetailDto(20L, "Jane", "Doe");
	}

	// ==================== CREATE OPERATIONS ====================

	@Test
	void createMedicalHistoryEntry_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
		when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
		when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(medicalHistory);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		MedicalHistoryResponseDto result = medicalHistoryService.createMedicalHistoryEntry(1L, requestDto, null);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
		verify(treatmentRepository).save(argThat(t -> t.getCompletedSessions() == 3));
	}

	@Test
	void createMedicalHistoryEntry_DentistNotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> medicalHistoryService.createMedicalHistoryEntry(1L, requestDto, null));

		assertTrue(exception.getMessage().contains("No dentist found"));
	}

	@Test
	void createMedicalHistoryEntry_PatientNotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> medicalHistoryService.createMedicalHistoryEntry(1L, requestDto, null));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	@Test
	void createMedicalHistoryEntry_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> medicalHistoryService.createMedicalHistoryEntry(1L, requestDto, null));

		assertTrue(exception.getMessage().contains("Patient does not belong to this dentist"));
	}

	@Test
	void createMedicalHistoryEntry_PatientNotActive() {
		// Arrange
		patient.setActive(false);
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> medicalHistoryService.createMedicalHistoryEntry(1L, requestDto, null));

		assertTrue(exception.getMessage().contains("The patient is not active"));
	}

	// ==================== READ OPERATIONS ====================

	@Test
	void getMedicalHistoryByPatient_Success() {
		// Arrange
		when(medicalHistoryRepository.findByPatientIdAndActiveTrue(2L))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.getMedicalHistoryByPatient(2L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getMedicalHistoryByDentistAndPatient_Success() {
		// Arrange
		when(medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrue(1L, 2L))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.getMedicalHistoryByDentistAndPatient(1L, 2L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getMedicalHistoryEntryById_Success() {
		// Arrange
		when(medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		MedicalHistoryResponseDto result = medicalHistoryService.getMedicalHistoryEntryById(1L, 1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	void getMedicalHistoryEntryById_NotFound() {
		// Arrange
		when(medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> medicalHistoryService.getMedicalHistoryEntryById(1L, 1L));

		assertTrue(exception.getMessage().contains("No clinical history entry found"));
	}

	@Test
	void getMedicalHistoryEntryByIdForPatient_Success() {
		// Arrange
		when(medicalHistoryRepository.findByIdAndPatientIdAndActiveTrue(1L, 2L))
				.thenReturn(Optional.of(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		MedicalHistoryResponseDto result = medicalHistoryService.getMedicalHistoryEntryByIdForPatient(1L, 2L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	// ==================== UPDATE OPERATIONS ====================

	@Test
	void updateMedicalHistoryEntry_Success() {
		// Arrange
		when(medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(medicalHistory));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
		when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
		when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(medicalHistory);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		MedicalHistoryResponseDto result = medicalHistoryService.updateMedicalHistoryEntry(1L, 1L, requestDto, null);

		// Assert
		assertNotNull(result);
		verify(medicalHistoryRepository).save(any(MedicalHistory.class));
	}

	// ==================== DELETE OPERATIONS ====================

	@Test
	void deleteMedicalHistoryEntry_Success() {
		// Arrange
		medicalHistory.setFileUrl("/files/test.pdf");
		when(medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(medicalHistory));
		when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(medicalHistory);

		// Act
		medicalHistoryService.deleteMedicalHistoryEntry(1L, 1L);

		// Assert
		verify(medicalHistoryRepository).save(argThat(mh -> !mh.getActive()));
		verify(fileStorageService).deleteFile("/files/test.pdf");
	}

	@Test
	void deleteMedicalHistoryEntry_WithoutFile() {
		// Arrange
		medicalHistory.setFileUrl(null);
		when(medicalHistoryRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(medicalHistory));
		when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(medicalHistory);

		// Act
		medicalHistoryService.deleteMedicalHistoryEntry(1L, 1L);

		// Assert
		verify(medicalHistoryRepository).save(argThat(mh -> !mh.getActive()));
		verify(fileStorageService, never()).deleteFile(anyString());
	}

	// ==================== SEARCH OPERATIONS ====================

	@Test
	void searchByText_WithSearchText() {
		// Arrange
		when(medicalHistoryRepository.findByPatientIdAndActiveTrueAndDescriptionContaining(2L, "checkup"))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.searchByText(2L, "checkup");

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void searchByText_EmptySearchText() {
		// Arrange
		when(medicalHistoryRepository.findByPatientIdAndActiveTrue(2L))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.searchByText(2L, "   ");

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void searchByText_WithDentistAndPatient() {
		// Arrange
		when(medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrueAndDescriptionContaining(1L, 2L, "checkup"))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.searchByText(1L, 2L, "checkup");

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void searchByDate_Success() {
		// Arrange
		LocalDate date = LocalDate.now();
		when(medicalHistoryRepository.findByPatientIdAndActiveTrueAndEntryDate(2L, date))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.searchByDate(2L, date);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void searchByDateRange_Success() {
		// Arrange
		LocalDate start = LocalDate.now().minusDays(7);
		LocalDate end = LocalDate.now();
		when(medicalHistoryRepository.findByPatientIdAndActiveTrueAndEntryDateBetween(2L, start, end))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.searchByDateRange(2L, start, end);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void searchByDateRange_WithDentist() {
		// Arrange
		LocalDate start = LocalDate.now().minusDays(7);
		LocalDate end = LocalDate.now();
		when(medicalHistoryRepository.findByDentistIdAndPatientIdAndActiveTrueAndEntryDateBetween(1L, 2L, start, end))
				.thenReturn(Arrays.asList(medicalHistory));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<MedicalHistoryResponseDto> result = medicalHistoryService.searchByDateRange(1L, 2L, start, end);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	// Helper method
	private UserDetailDto createUserDetailDto(Long userId, String firstName, String lastName) {
		UserDetailDto dto = new UserDetailDto();
		dto.setUserId(userId);
		dto.setFirstName(firstName);
		dto.setLastName(lastName);
		dto.setEmail(firstName.toLowerCase() + "@example.com");
		return dto;
	}
}