package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.dentist.CreateDentistFromUserRequest;
import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.response.PagedResponse;
import com.dentalCare.be_core.dtos.response.dentist.DentistPatientsResponseDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DentistServiceImplTest {

	@Mock
	private DentistRepository dentistRepository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@InjectMocks
	private DentistServiceImpl dentistService;

	private Dentist dentist;
	private Patient patient;
	private UserDetailDto dentistUser;
	private UserDetailDto patientUser;
	private DentistRequestDto dentistRequestDto;
	private CreateDentistFromUserRequest createDentistRequest;
	private PatientRequestDto patientRequestDto;
	private DentistUpdateRequestDto updateRequestDto;

	@BeforeEach
	void setUp() {
		dentist = new Dentist();
		dentist.setId(1L);
		dentist.setUserId(10L);
		dentist.setLicenseNumber("LIC123");
		dentist.setSpecialty("Orthodontics");
		dentist.setActive(true);

		patient = new Patient();
		patient.setId(2L);
		patient.setUserId(20L);
		patient.setDni("12345678A");
		patient.setDentist(dentist);
		patient.setActive(true);

		dentistUser = createUserDetailDto(10L, "Dr. John", "Smith");
		patientUser = createUserDetailDto(20L, "Jane", "Doe");

		dentistRequestDto = new DentistRequestDto();
		dentistRequestDto.setUserId(10L);
		dentistRequestDto.setLicenseNumber("LIC123");
		dentistRequestDto.setSpecialty("Orthodontics");

		createDentistRequest = new CreateDentistFromUserRequest();
		createDentistRequest.setUserId(10L);
		createDentistRequest.setLicenseNumber("LIC123");
		createDentistRequest.setSpecialty("Orthodontics");

		patientRequestDto = new PatientRequestDto();
		patientRequestDto.setUserId(20L);
		patientRequestDto.setDni("12345678A");
		patientRequestDto.setBirthDate(LocalDate.of(1990, 1, 1));

		updateRequestDto = new DentistUpdateRequestDto();
		updateRequestDto.setLicenseNumber("LIC456");
		updateRequestDto.setSpecialty("Endodontics");
		updateRequestDto.setActive(true);
	}

	// ==================== CREATE OPERATIONS ====================

	@Test
	void createDentist_Success() {
		// Arrange
		when(dentistRepository.existsByLicenseNumber("LIC123")).thenReturn(false);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);

		// Act
		DentistResponseDto result = dentistService.createDentist(dentistRequestDto);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("LIC123", result.getLicenseNumber());
		verify(dentistRepository).save(any(Dentist.class));
	}

	@Test
	void createDentist_LicenseAlreadyExists() {
		// Arrange
		when(dentistRepository.existsByLicenseNumber("LIC123")).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.createDentist(dentistRequestDto));

		assertTrue(exception.getMessage().contains("already a dentist with the license"));
	}

	@Test
	void createDentist_UserNotFound() {
		// Arrange
		when(dentistRepository.existsByLicenseNumber("LIC123")).thenReturn(false);
		when(userServiceClient.getUserById(10L)).thenReturn(null);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.createDentist(dentistRequestDto));

		assertTrue(exception.getMessage().contains("User not found"));
	}

	@Test
	void createDentistFromUser_Success() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(dentistRepository.existsByUserId(10L)).thenReturn(false);
		when(dentistRepository.existsByLicenseNumber("LIC123")).thenReturn(false);
		when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);

		// Act
		DentistResponseDto result = dentistService.createDentistFromUser(createDentistRequest);

		// Assert
		assertNotNull(result);
		verify(dentistRepository).save(any(Dentist.class));
	}

	@Test
	void createDentistFromUser_DentistAlreadyExistsForUser() {
		// Arrange
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(dentistRepository.existsByUserId(10L)).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.createDentistFromUser(createDentistRequest));

		assertTrue(exception.getMessage().contains("Dentist already exists for user"));
	}

	@Test
	void createPatientForDentist_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(patientRepository.existsByDni("12345678A")).thenReturn(false);
		when(patientRepository.save(any(Patient.class))).thenReturn(patient);

		// Act
		PatientResponseDto result = dentistService.createPatientForDentist(1L, patientRequestDto);

		// Assert
		assertNotNull(result);
		assertEquals("12345678A", result.getDni());
		verify(patientRepository).save(any(Patient.class));
	}

	@Test
	void createPatientForDentist_DniAlreadyExists() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(patientRepository.existsByDni("12345678A")).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.createPatientForDentist(1L, patientRequestDto));

		assertTrue(exception.getMessage().contains("already a patient with the DNI"));
	}

	// ==================== READ OPERATIONS ====================

	@Test
	void searchById_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);

		// Act
		DentistResponseDto result = dentistService.searchById(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Dr. John", result.getFirstName());
	}

	@Test
	void searchById_NotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.searchById(1L));

		assertTrue(exception.getMessage().contains("No dentist found"));
	}

	@Test
	void searchByLicenseNumber_Success() {
		// Arrange
		when(dentistRepository.findByLicenseNumber("LIC123")).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);

		// Act
		DentistResponseDto result = dentistService.searchByLicenseNumber("LIC123");

		// Assert
		assertNotNull(result);
		assertEquals("LIC123", result.getLicenseNumber());
	}

	@Test
	void searchByLicenseNumber_NotFound() {
		// Arrange
		when(dentistRepository.findByLicenseNumber("LIC123")).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.searchByLicenseNumber("LIC123"));

		assertTrue(exception.getMessage().contains("No licensed dentist found"));
	}

	@Test
	void findAllActive_Success() {
		// Arrange
		when(dentistRepository.findAllActive()).thenReturn(Arrays.asList(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);

		// Act
		List<DentistResponseDto> result = dentistService.findAllActive();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void searchBySpecialty_Success() {
		// Arrange
		when(dentistRepository.findActiveBySpecialty("Orthodontics")).thenReturn(Arrays.asList(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);

		// Act
		List<DentistResponseDto> result = dentistService.searchBySpecialty("Orthodontics");

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Orthodontics", result.get(0).getSpecialty());
	}

	@Test
	void getPatientsByDentistId_Success() {
		// Arrange
		dentist.setPatients(Arrays.asList(patient));
		when(dentistRepository.findByIdWithPatients(1L)).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		DentistPatientsResponseDto result = dentistService.getPatientsByDentistId(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getDentistId());
		assertEquals(1, result.getPatients().size());
	}

	@Test
	void getActivePatientsByDentistId_Success() {
		// Arrange
		Patient inactivePatient = new Patient();
		inactivePatient.setId(3L);
		inactivePatient.setUserId(30L);
		inactivePatient.setActive(false);

		dentist.setPatients(Arrays.asList(patient, inactivePatient));
		when(dentistRepository.findByIdWithActivePatients(1L)).thenReturn(Optional.of(dentist));
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		DentistPatientsResponseDto result = dentistService.getActivePatientsByDentistId(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.getPatients().size());
		assertTrue(result.getPatients().get(0).getActive());
	}

	@Test
	void getAvailablePatientUsers_Success() {
		// Arrange
		patient.setDentist(null);
		when(patientRepository.findAvailablePatients()).thenReturn(Arrays.asList(patient));
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);

		// Act
		List<PatientResponseDto> result = dentistService.getAvailablePatientUsers();

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Jane", result.get(0).getFirstName());
	}

	@Test
	void getAvailablePatientUsersPaged_SortByFirstName() {
		// Arrange
		Patient patient2 = new Patient();
		patient2.setId(3L);
		patient2.setUserId(30L);
		patient2.setDni("87654321B");
		patient2.setActive(true);

		UserDetailDto user2 = createUserDetailDto(30L, "Alice", "Brown");

		Page<Patient> patientPage = new PageImpl<>(Arrays.asList(patient, patient2));
		when(patientRepository.findAvailablePatients(any(Pageable.class))).thenReturn(patientPage);
		when(userServiceClient.getUserById(20L)).thenReturn(patientUser);
		when(userServiceClient.getUserById(30L)).thenReturn(user2);

		// Act
		PagedResponse<PatientResponseDto> result = dentistService.getAvailablePatientUsersPaged(0, 10, "firstName", "asc");

		// Assert
		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		assertEquals("Alice", result.getContent().get(0).getFirstName());
	}

	@Test
	void getDentistIdByUserId_Success() {
		// Arrange
		when(dentistRepository.findByUserId(10L)).thenReturn(Optional.of(dentist));

		// Act
		Long result = dentistService.getDentistIdByUserId(10L);

		// Assert
		assertEquals(1L, result);
	}

	@Test
	void getDentistIdByUserId_NotFound() {
		// Arrange
		when(dentistRepository.findByUserId(10L)).thenReturn(Optional.empty());

		// Act
		Long result = dentistService.getDentistIdByUserId(10L);

		// Assert
		assertNull(result);
	}

	// ==================== UPDATE OPERATIONS ====================

	@Test
	void updateDentist_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(dentistRepository.existsByLicenseNumber("LIC456")).thenReturn(false);
		when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);

		// Act
		DentistResponseDto result = dentistService.updateDentist(1L, updateRequestDto);

		// Assert
		assertNotNull(result);
		verify(dentistRepository).save(any(Dentist.class));
	}

	@Test
	void updateDentist_SameLicense() {
		// Arrange
		updateRequestDto.setLicenseNumber("LIC123");
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);
		when(userServiceClient.getUserById(10L)).thenReturn(dentistUser);

		// Act
		DentistResponseDto result = dentistService.updateDentist(1L, updateRequestDto);

		// Assert
		assertNotNull(result);
		verify(dentistRepository, never()).existsByLicenseNumber(anyString());
	}

	@Test
	void updateDentist_LicenseAlreadyExists() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(dentistRepository.existsByLicenseNumber("LIC456")).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.updateDentist(1L, updateRequestDto));

		assertTrue(exception.getMessage().contains("already a dentist with the license"));
	}

	// ==================== DELETE OPERATIONS ====================

	@Test
	void deleteDentist_Success() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));

		// Act
		dentistService.deleteDentist(1L);

		// Assert
		verify(dentistRepository).delete(dentist);
	}

	@Test
	void deleteDentist_NotFound() {
		// Arrange
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> dentistService.deleteDentist(1L));

		assertTrue(exception.getMessage().contains("No dentist found"));
	}

	// ==================== UTILITY OPERATIONS ====================

	@Test
	void existsByLicense_True() {
		// Arrange
		when(dentistRepository.existsByLicenseNumber("LIC123")).thenReturn(true);

		// Act
		boolean result = dentistService.existsByLicense("LIC123");

		// Assert
		assertTrue(result);
	}

	@Test
	void existsByLicense_False() {
		// Arrange
		when(dentistRepository.existsByLicenseNumber("LIC123")).thenReturn(false);

		// Act
		boolean result = dentistService.existsByLicense("LIC123");

		// Assert
		assertFalse(result);
	}

	@Test
	void existsByEmail_ReturnsFalse() {
		// Act
		boolean result = dentistService.existsByEmail("test@email.com");

		// Assert
		assertFalse(result);
	}

	@Test
	void countActiveDentist_Success() {
		// Arrange
		when(dentistRepository.countActiveDentist()).thenReturn(5L);

		// Act
		long result = dentistService.countActiveDentist();

		// Assert
		assertEquals(5L, result);
	}

	// Helper method
	private UserDetailDto createUserDetailDto(Long userId, String firstName, String lastName) {
		UserDetailDto dto = new UserDetailDto();
		dto.setUserId(userId);
		dto.setFirstName(firstName);
		dto.setLastName(lastName);
		dto.setEmail(firstName.toLowerCase() + "@example.com");
		dto.setPhone("+1234567890");
		dto.setAddress("123 Main St");
		return dto;
	}
}