package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.treatment.TreatmentRequestDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.entities.*;
import com.dentalCare.be_core.repositories.*;
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
class TreatmentServiceImplTest {

	@Mock
	private TreatmentRepository treatmentRepository;

	@Mock
	private DentistRepository dentistRepository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private MedicalHistoryRepository medicalHistoryRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private ModelMapperUtils modelMapperUtils;

	@InjectMocks
	private TreatmentServiceImpl treatmentService;

	private Dentist dentist;
	private Patient patient;
	private Treatment treatment;
	private TreatmentRequestDto requestDto;
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
		patient.setDni("12345678");
		patient.setDentist(dentist);
		patient.setActive(true);

		treatment = new Treatment();
		treatment.setId(1L);
		treatment.setDentist(dentist);
		treatment.setPatient(patient);
		treatment.setName("Root Canal");
		treatment.setDescription("Root canal treatment");
		treatment.setStartDate(LocalDate.now());
		treatment.setStatus(TreatmentStatus.EN_CURSO);
		treatment.setTotalSessions(5);
		treatment.setCompletedSessions(2);
		treatment.setActive(true);

		requestDto = new TreatmentRequestDto();
		requestDto.setPatientId(2L);
		requestDto.setName("Root Canal");
		requestDto.setDescription("Root canal treatment");
		requestDto.setStartDate(LocalDate.now());
		requestDto.setTotalSessions(5);

		dentistUser = createUserDetailDto(10L, "Dr. John", "Smith");
		patientUser = createUserDetailDto(20L, "Jane", "Doe");
	}

	@Test
	void createTreatment_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(modelMapperUtils.map(any(TreatmentRequestDto.class), eq(Treatment.class)))
				.thenReturn(treatment);
		when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Treatment.class), eq(TreatmentResponseDto.class)))
				.thenReturn(new TreatmentResponseDto());

		// Act
		TreatmentResponseDto result = treatmentService.createTreatment(1L, requestDto);

		// Assert
		assertNotNull(result);
		verify(dentistRepository).findById(1L);
		verify(patientRepository).findById(2L);
		verify(treatmentRepository).save(any(Treatment.class));
	}

	@Test
	void createTreatment_DentistNotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> treatmentService.createTreatment(1L, requestDto));

		assertTrue(exception.getMessage().contains("No dentist found"));
	}

	@Test
	void createTreatment_PatientNotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> treatmentService.createTreatment(1L, requestDto));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	@Test
	void createTreatment_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> treatmentService.createTreatment(1L, requestDto));

		assertTrue(exception.getMessage().contains("Patient does not belong to this dentist"));
	}

	@Test
	void createTreatment_PatientNotActive() {
		// Arrange
		patient.setActive(false);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> treatmentService.createTreatment(1L, requestDto));

		assertTrue(exception.getMessage().contains("The patient is not active"));
	}

	@Test
	void getTreatmentsByPatient_Success() {
		// Arrange
		List<Treatment> treatments = Arrays.asList(treatment);
		when(treatmentRepository.findByPatientIdAndActiveTrue(2L)).thenReturn(treatments);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Treatment.class), eq(TreatmentResponseDto.class)))
				.thenReturn(new TreatmentResponseDto());

		// Act
		List<TreatmentResponseDto> result = treatmentService.getTreatmentsByPatient(2L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(treatmentRepository).findByPatientIdAndActiveTrue(2L);
	}

	@Test
	void getTreatmentDetailById_Success() {
		// Arrange
		when(treatmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(treatment));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(medicalHistoryRepository.findByPatientIdAndActiveTrue(2L))
				.thenReturn(Arrays.asList());

		// Act
		TreatmentDetailResponseDto result = treatmentService.getTreatmentDetailById(1L, 1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(treatmentRepository).findByIdAndDentistIdAndActiveTrue(1L, 1L);
	}

	@Test
	void updateTreatmentStatus_Success() {
		// Arrange
		when(treatmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(treatment));
		when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(modelMapperUtils.map(any(Treatment.class), eq(TreatmentResponseDto.class)))
				.thenReturn(new TreatmentResponseDto());

		// Act
		TreatmentResponseDto result = treatmentService.updateTreatmentStatus(1L, 1L, "COMPLETADO");

		// Assert
		assertNotNull(result);
		verify(treatmentRepository).save(any(Treatment.class));
	}

	@Test
	void updateTreatmentStatus_InvalidStatus() {
		// Arrange
		when(treatmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(treatment));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> treatmentService.updateTreatmentStatus(1L, 1L, "INVALID_STATUS"));

		assertTrue(exception.getMessage().contains("Invalid status"));
	}

	@Test
	void deleteTreatment_Success() {
		// Arrange
		when(treatmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(treatment));
		when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);

		// Act
		treatmentService.deleteTreatment(1L, 1L);

		// Assert
		verify(treatmentRepository).save(argThat(t -> !t.getActive()));
	}

	@Test
	void incrementTreatmentSessions_Success() {
		// Arrange
		when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
		when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);

		// Act
		treatmentService.incrementTreatmentSessions(1L);

		// Assert
		verify(treatmentRepository).save(argThat(t -> t.getCompletedSessions() == 3));
	}

	@Test
	void getTreatmentEntityById_Success() {
		// Arrange
		when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));

		// Act
		Treatment result = treatmentService.getTreatmentEntityById(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	void getTreatmentEntityById_NotFound() {
		// Arrange
		when(treatmentRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> treatmentService.getTreatmentEntityById(1L));

		assertTrue(exception.getMessage().contains("No treatment found"));
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