package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.prescription.PrescriptionRequestDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.entities.Prescription;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.repositories.PrescriptionRepository;
import com.dentalCare.be_core.services.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

	@Mock
	private PrescriptionRepository prescriptionRepository;

	@Mock
	private DentistRepository dentistRepository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private ModelMapperUtils modelMapperUtils;

	@InjectMocks
	private PrescriptionServiceImpl prescriptionService;

	private Dentist dentist;
	private Patient patient;
	private Prescription prescription;
	private PrescriptionRequestDto requestDto;
	private UserDetailDto dentistUser;
	private UserDetailDto patientUser;

	@BeforeEach
	void setUp() {
		dentist = new Dentist();
		dentist.setId(1L);
		dentist.setUserId(10L);
		dentist.setLicenseNumber("LIC123");
		dentist.setSpecialty("Orthodontics");

		patient = new Patient();
		patient.setId(2L);
		patient.setUserId(20L);
		patient.setDni("12345678");
		patient.setDentist(dentist);
		patient.setActive(true);

		prescription = new Prescription();
		prescription.setId(1L);
		prescription.setDentist(dentist);
		prescription.setPatient(patient);
		prescription.setPrescriptionDate(LocalDate.now());
		prescription.setObservations("Take with food");
		prescription.setMedications("Amoxicillin 500mg");
		prescription.setActive(true);

		requestDto = new PrescriptionRequestDto();
		requestDto.setPatientId(2L);
		requestDto.setPrescriptionDate(LocalDate.now());
		requestDto.setObservations("Take with food");
		requestDto.setMedications("Amoxicillin 500mg");

		dentistUser = createUserDetailDto(10L, "Dr. John", "Smith");
		patientUser = createUserDetailDto(20L, "Jane", "Doe");
	}

	@Test
	void createPrescriptionForDentist_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(modelMapperUtils.map(any(PrescriptionRequestDto.class), eq(Prescription.class)))
				.thenReturn(prescription);
		when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Prescription.class), eq(PrescriptionResponseDto.class)))
				.thenReturn(new PrescriptionResponseDto());

		// Act
		PrescriptionResponseDto result = prescriptionService.createPrescriptionForDentist(1L, requestDto);

		// Assert
		assertNotNull(result);
		verify(dentistRepository).findById(1L);
		verify(patientRepository).findById(2L);
		verify(prescriptionRepository).save(any(Prescription.class));
	}

	@Test
	void createPrescriptionForDentist_DentistNotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.createPrescriptionForDentist(1L, requestDto));

		assertTrue(exception.getMessage().contains("No dentist found"));
	}

	@Test
	void createPrescriptionForDentist_PatientNotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.createPrescriptionForDentist(1L, requestDto));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	@Test
	void createPrescriptionForDentist_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.createPrescriptionForDentist(1L, requestDto));

		assertTrue(exception.getMessage().contains("Patient does not belong to this dentist"));
	}

	@Test
	void createPrescriptionForDentist_PatientNotActive() {
		// Arrange
		patient.setActive(false);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.createPrescriptionForDentist(1L, requestDto));

		assertTrue(exception.getMessage().contains("The patient is not active"));
	}

	@Test
	void getPrescriptionsByDentistId_Success() {
		// Arrange
		List<Prescription> prescriptions = Arrays.asList(prescription);
		when(prescriptionRepository.findByDentistIdAndActiveTrue(1L)).thenReturn(prescriptions);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Prescription.class), eq(PrescriptionResponseDto.class)))
				.thenReturn(new PrescriptionResponseDto());

		// Act
		List<PrescriptionResponseDto> result = prescriptionService.getPrescriptionsByDentistId(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(prescriptionRepository).findByDentistIdAndActiveTrue(1L);
	}

	@Test
	void getPrescriptionsByDentistIdAndPatientId_Success() {
		// Arrange
		List<Prescription> prescriptions = Arrays.asList(prescription);
		when(prescriptionRepository.findByDentistIdAndPatientIdAndActiveTrue(1L, 2L))
				.thenReturn(prescriptions);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Prescription.class), eq(PrescriptionResponseDto.class)))
				.thenReturn(new PrescriptionResponseDto());

		// Act
		List<PrescriptionResponseDto> result = prescriptionService
				.getPrescriptionsByDentistIdAndPatientId(1L, 2L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getPrescriptionByIdAndDentistId_Success() {
		// Arrange
		when(prescriptionRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(prescription));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Prescription.class), eq(PrescriptionResponseDto.class)))
				.thenReturn(new PrescriptionResponseDto());

		// Act
		PrescriptionResponseDto result = prescriptionService.getPrescriptionByIdAndDentistId(1L, 1L);

		// Assert
		assertNotNull(result);
		verify(prescriptionRepository).findByIdAndDentistIdAndActiveTrue(1L, 1L);
	}

	@Test
	void getPrescriptionByIdAndDentistId_NotFound() {
		// Arrange
		when(prescriptionRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.getPrescriptionByIdAndDentistId(1L, 1L));

		assertTrue(exception.getMessage().contains("No prescription found"));
	}

	@Test
	void updatePrescription_Success() {
		// Arrange
		when(prescriptionRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(prescription));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Prescription.class), eq(PrescriptionResponseDto.class)))
				.thenReturn(new PrescriptionResponseDto());

		// Act
		PrescriptionResponseDto result = prescriptionService.updatePrescription(1L, 1L, requestDto);

		// Assert
		assertNotNull(result);
		verify(prescriptionRepository).save(any(Prescription.class));
	}

	@Test
	void deletePrescription_Success() {
		// Arrange
		when(prescriptionRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(prescription));

		// Act
		prescriptionService.deletePrescription(1L, 1L);

		// Assert
		verify(prescriptionRepository).delete(prescription);
	}

	@Test
	void getPrescriptionsByPatientId_Success() {
		// Arrange
		List<Prescription> prescriptions = Arrays.asList(prescription);
		when(prescriptionRepository.findByPatientIdAndActiveTrue(2L)).thenReturn(prescriptions);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Prescription.class), eq(PrescriptionResponseDto.class)))
				.thenReturn(new PrescriptionResponseDto());

		// Act
		List<PrescriptionResponseDto> result = prescriptionService.getPrescriptionsByPatientId(2L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void countPrescriptionsByDentistId_Success() {
		// Arrange
		when(prescriptionRepository.countByDentistIdAndActiveTrue(1L)).thenReturn(5L);

		// Act
		long result = prescriptionService.countPrescriptionsByDentistId(1L);

		// Assert
		assertEquals(5L, result);
		verify(prescriptionRepository).countByDentistIdAndActiveTrue(1L);
	}

	@Test
	void countPrescriptionsByPatientId_Success() {
		// Arrange
		when(prescriptionRepository.countByPatientIdAndActiveTrue(2L)).thenReturn(3L);

		// Act
		long result = prescriptionService.countPrescriptionsByPatientId(2L);

		// Assert
		assertEquals(3L, result);
		verify(prescriptionRepository).countByPatientIdAndActiveTrue(2L);
	}

	@Test
	void getPrescriptionEntityById_WithDentist_Success() {
		// Arrange
		when(prescriptionRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(prescription));

		// Act
		Prescription result = prescriptionService.getPrescriptionEntityById(1L, 1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	void getPrescriptionEntityById_Success() {
		// Arrange
		when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));

		// Act
		Prescription result = prescriptionService.getPrescriptionEntityById(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	void getPrescriptionEntityById_NotFound() {
		// Arrange
		when(prescriptionRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.getPrescriptionEntityById(1L));

		assertTrue(exception.getMessage().contains("No prescription found"));
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