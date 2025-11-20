package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.patient.CreatePatientFromUserRequest;
import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private DentistRepository dentistRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@InjectMocks
	private PatientServiceImpl patientService;

	private Patient patient;
	private UserDetailDto userDetail;
	private CreatePatientFromUserRequest createRequest;
	private PatientUpdateRequestDto updateRequest;
	private Dentist dentist;

	@BeforeEach
	void setUp() {
		patient = new Patient();
		patient.setId(1L);
		patient.setUserId(10L);
		patient.setDni("12345678A");
		patient.setActive(true);
		patient.setDentist(null);

		userDetail = new UserDetailDto();
		userDetail.setUserId(10L);
		userDetail.setFirstName("John");
		userDetail.setLastName("Doe");
		userDetail.setEmail("john.doe@email.com");
		userDetail.setPhone("+1234567890");
		userDetail.setAddress("123 Main St");

		createRequest = new CreatePatientFromUserRequest();
		createRequest.setUserId(10L);
		createRequest.setDni("12345678A");

		updateRequest = new PatientUpdateRequestDto();
		updateRequest.setDni("87654321B");
		updateRequest.setActive(true);

		dentist = new Dentist();
		dentist.setId(1L);
		dentist.setUserId(20L);
		dentist.setLicenseNumber("LIC123");
	}

	// ==================== CREATE OPERATIONS ====================

	@Test
	void createPatientFromUser_Success() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);
		when(patientRepository.existsByUserId(10L)).thenReturn(false);
		when(patientRepository.existsByDni("12345678A")).thenReturn(false);
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);

		// Act
		PatientResponseDto result = patientService.createPatientFromUser(createRequest);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals(10L, result.getUserId());
		assertEquals("12345678A", result.getDni());
		assertTrue(result.getActive());
		verify(patientRepository).save(any(Patient.class));
	}

	@Test
	void createPatientFromUser_UserNotFound() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(null);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.createPatientFromUser(createRequest));

		assertTrue(exception.getMessage().contains("User not found"));
	}

	@Test
	void createPatientFromUser_PatientAlreadyExistsForUser() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);
		when(patientRepository.existsByUserId(10L)).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.createPatientFromUser(createRequest));

		assertTrue(exception.getMessage().contains("Patient already exists for user"));
	}

	@Test
	void createPatientFromUser_DniAlreadyExists() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);
		when(patientRepository.existsByUserId(10L)).thenReturn(false);
		when(patientRepository.existsByDni("12345678A")).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.createPatientFromUser(createRequest));

		assertTrue(exception.getMessage().contains("already a patient with the DNI"));
	}

	// ==================== READ OPERATIONS ====================

	@Test
	void searchById_Success() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.searchById(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("John", result.getFirstName());
		assertEquals("Doe", result.getLastName());
	}

	@Test
	void searchById_NotFound() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.searchById(1L));

		assertTrue(exception.getMessage().contains("No patient found with ID"));
	}

	@Test
	void searchByDni_Success() {
		// Arrange
		when(patientRepository.findByDni("12345678A")).thenReturn(Optional.of(patient));
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.searchByDni("12345678A");

		// Assert
		assertNotNull(result);
		assertEquals("12345678A", result.getDni());
	}

	@Test
	void searchByDni_NotFound() {
		// Arrange
		when(patientRepository.findByDni("12345678A")).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.searchByDni("12345678A"));

		assertTrue(exception.getMessage().contains("No patient found with DNI"));
	}

	@Test
	void findAllActive_Success() {
		// Arrange
		Patient patient2 = new Patient();
		patient2.setId(2L);
		patient2.setUserId(11L);
		patient2.setDni("11111111B");
		patient2.setActive(true);

		UserDetailDto userDetail2 = new UserDetailDto();
		userDetail2.setUserId(11L);
		userDetail2.setFirstName("Jane");
		userDetail2.setLastName("Smith");

		when(patientRepository.findAllActive()).thenReturn(Arrays.asList(patient, patient2));
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);
		when(userServiceClient.getUserById(11L)).thenReturn(userDetail2);

		// Act
		List<PatientResponseDto> result = patientService.findAllActive();

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("John", result.get(0).getFirstName());
		assertEquals("Jane", result.get(1).getFirstName());
	}

	// ==================== UPDATE OPERATIONS ====================

	@Test
	void updatePatient_Success() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(patientRepository.existsByDni("87654321B")).thenReturn(false);
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.updatePatient(1L, updateRequest);

		// Assert
		assertNotNull(result);
		verify(patientRepository).save(any(Patient.class));
		verify(patientRepository).flush();
	}

	@Test
	void updatePatient_DniNotChanged() {
		// Arrange
		updateRequest.setDni("12345678A"); // Same DNI
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.updatePatient(1L, updateRequest);

		// Assert
		assertNotNull(result);
		verify(patientRepository, never()).existsByDni(anyString());
	}

	@Test
	void updatePatient_EmptyDni() {
		// Arrange
		updateRequest.setDni("   ");
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.updatePatient(1L, updateRequest);

		// Assert
		assertNotNull(result);
		verify(patientRepository, never()).existsByDni(anyString());
	}

	@Test
	void updatePatient_NullDni() {
		// Arrange
		updateRequest.setDni(null);
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.updatePatient(1L, updateRequest);

		// Assert
		assertNotNull(result);
		verify(patientRepository, never()).existsByDni(anyString());
	}

	@Test
	void updatePatient_DniAlreadyExists() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(patientRepository.existsByDni("87654321B")).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.updatePatient(1L, updateRequest));

		assertTrue(exception.getMessage().contains("already a patient with the DNI"));
	}

	@Test
	void updatePatient_OnlyActiveField() {
		// Arrange
		updateRequest.setDni(null);
		updateRequest.setActive(false);

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.updatePatient(1L, updateRequest);

		// Assert
		assertNotNull(result);
		verify(patientRepository).save(argThat(p -> !p.getActive()));
	}

	// ==================== DELETE OPERATIONS ====================

	@Test
	void deletePatient_Success() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		// Act
		patientService.deletePatient(1L);

		// Assert
		verify(patientRepository).delete(patient);
	}

	@Test
	void deletePatient_NotFound() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.deletePatient(1L));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	// ==================== UTILITY OPERATIONS ====================

	@Test
	void existsByDni_True() {
		// Arrange
		when(patientRepository.existsByDni("12345678A")).thenReturn(true);

		// Act
		boolean result = patientService.existsByDni("12345678A");

		// Assert
		assertTrue(result);
	}

	@Test
	void existsByDni_False() {
		// Arrange
		when(patientRepository.existsByDni("12345678A")).thenReturn(false);

		// Act
		boolean result = patientService.existsByDni("12345678A");

		// Assert
		assertFalse(result);
	}

	@Test
	void existsByEmail_ReturnsFalse() {
		// Act
		boolean result = patientService.existsByEmail("test@email.com");

		// Assert
		assertFalse(result);
	}

	@Test
	void countActivePatient_Success() {
		// Arrange
		when(patientRepository.countActivePatient()).thenReturn(5L);

		// Act
		long result = patientService.countActivePatient();

		// Assert
		assertEquals(5L, result);
	}

	@Test
	void getPatientIdByUserId_Success() {
		// Arrange
		when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));

		// Act
		Long result = patientService.getPatientIdByUserId(10L);

		// Assert
		assertEquals(1L, result);
	}

	@Test
	void getPatientIdByUserId_NotFound() {
		// Arrange
		when(patientRepository.findByUserId(10L)).thenReturn(Optional.empty());

		// Act
		Long result = patientService.getPatientIdByUserId(10L);

		// Assert
		assertNull(result);
	}

	// ==================== ASSIGN DENTIST ====================

	@Test
	void assignDentistToPatient_Success() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.assignDentistToPatient(1L, 1L);

		// Assert
		assertNotNull(result);
		verify(patientRepository).save(argThat(p -> p.getDentist() != null && p.getActive()));
	}

	@Test
	void assignDentistToPatient_PatientNotFound() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.assignDentistToPatient(1L, 1L));

		assertTrue(exception.getMessage().contains("No patient found"));
	}

	@Test
	void assignDentistToPatient_DentistNotFound() {
		// Arrange
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> patientService.assignDentistToPatient(1L, 1L));

		assertTrue(exception.getMessage().contains("Dentista con ID"));
	}

	@Test
	void assignDentistToPatient_AlreadyAssigned() {
		// Arrange
		patient.setDentist(dentist);
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.assignDentistToPatient(1L, 1L);

		// Assert
		assertNotNull(result);
		verify(patientRepository, never()).save(any(Patient.class));
	}

	@Test
	void assignDentistToPatient_ReassignDifferentDentist() {
		// Arrange
		Dentist oldDentist = new Dentist();
		oldDentist.setId(99L);
		patient.setDentist(oldDentist);

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);
		when(userServiceClient.getUserById(10L)).thenReturn(userDetail);

		// Act
		PatientResponseDto result = patientService.assignDentistToPatient(1L, 1L);

		// Assert
		assertNotNull(result);
		verify(patientRepository).save(argThat(p ->
				p.getDentist().getId().equals(1L) && p.getActive()
		));
	}
}