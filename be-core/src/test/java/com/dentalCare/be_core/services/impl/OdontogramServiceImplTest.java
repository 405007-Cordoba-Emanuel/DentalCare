package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.request.odontogram.OdontogramRequestDto;
import com.dentalCare.be_core.dtos.response.odontogram.OdontogramResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Odontogram;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.OdontogramRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OdontogramServiceImplTest {

	@Mock
	private OdontogramRepository odontogramRepository;

	@Mock
	private PatientRepository patientRepository;

	@InjectMocks
	private OdontogramServiceImpl odontogramService;

	private Dentist dentist;
	private Patient patient;
	private Odontogram odontogram;
	private OdontogramRequestDto requestDto;

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

		odontogram = new Odontogram();
		odontogram.setId(1L);
		odontogram.setPatient(patient);
		odontogram.setDentitionType("PERMANENT");
		odontogram.setTeethData("{\"tooth_11\": {\"status\": \"healthy\"}}");
		odontogram.setIsActive(true);
		odontogram.setCreatedDatetime(LocalDateTime.now());

		requestDto = new OdontogramRequestDto();
		requestDto.setPatientId(2L);
		requestDto.setDentitionType("PERMANENT");
		requestDto.setTeethData("{\"tooth_11\": {\"status\": \"healthy\"}}");
	}

	// ==================== CREATE OPERATIONS ====================

	@Test
	void createOdontogram_Success() {
		// Arrange
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(odontogramRepository.save(any(Odontogram.class))).thenReturn(odontogram);

		// Act
		OdontogramResponseDto result = odontogramService.createOdontogram(1L, requestDto);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals(2L, result.getPatientId());
		assertEquals("PERMANENT", result.getDentitionType());
		assertTrue(result.getIsActive());
		verify(odontogramRepository).save(any(Odontogram.class));
	}

	@Test
	void createOdontogram_PatientNotFound() {
		// Arrange
		when(patientRepository.findById(2L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.createOdontogram(1L, requestDto));

		assertTrue(exception.getMessage().contains("No se encontró el paciente"));
	}

	@Test
	void createOdontogram_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.createOdontogram(1L, requestDto));

		assertTrue(exception.getMessage().contains("El paciente no pertenece a este dentista"));
	}

	@Test
	void createOdontogram_PatientNotActive() {
		// Arrange
		patient.setActive(false);
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.createOdontogram(1L, requestDto));

		assertTrue(exception.getMessage().contains("El paciente no está activo"));
	}

	@Test
	void createOdontogram_PatientActiveIsNull() {
		// Arrange
		patient.setActive(null);
		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.createOdontogram(1L, requestDto));

		assertTrue(exception.getMessage().contains("El paciente no está activo"));
	}

	// ==================== READ OPERATIONS ====================

	@Test
	void getOdontogramsByPatient_Success() {
		// Arrange
		Odontogram odontogram2 = new Odontogram();
		odontogram2.setId(2L);
		odontogram2.setPatient(patient);
		odontogram2.setDentitionType("DECIDUOUS");
		odontogram2.setTeethData("{\"tooth_51\": {\"status\": \"healthy\"}}");
		odontogram2.setIsActive(true);
		odontogram2.setCreatedDatetime(LocalDateTime.now());

		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
		when(odontogramRepository.findByPatientIdAndIsActiveTrueOrderByCreatedDatetimeDesc(2L))
				.thenReturn(Arrays.asList(odontogram, odontogram2));

		// Act
		List<OdontogramResponseDto> result = odontogramService.getOdontogramsByPatient(1L, 2L);

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("PERMANENT", result.get(0).getDentitionType());
		assertEquals("DECIDUOUS", result.get(1).getDentitionType());
	}

	@Test
	void getOdontogramsByPatient_PatientNotFound() {
		// Arrange
		when(patientRepository.findById(2L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.getOdontogramsByPatient(1L, 2L));

		assertTrue(exception.getMessage().contains("No se encontró el paciente"));
	}

	@Test
	void getOdontogramsByPatient_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.getOdontogramsByPatient(1L, 2L));

		assertTrue(exception.getMessage().contains("El paciente no pertenece a este dentista"));
	}

	@Test
	void getOdontogramById_Success() {
		// Arrange
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act
		OdontogramResponseDto result = odontogramService.getOdontogramById(1L, 1L);

		// Assert
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals(2L, result.getPatientId());
		verify(odontogramRepository).findById(1L);
	}

	@Test
	void getOdontogramById_NotFound() {
		// Arrange
		when(odontogramRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.getOdontogramById(1L, 1L));

		assertTrue(exception.getMessage().contains("No se encontró el odontograma"));
	}

	@Test
	void getOdontogramById_NotActive() {
		// Arrange
		odontogram.setIsActive(false);
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.getOdontogramById(1L, 1L));

		assertTrue(exception.getMessage().contains("El odontograma no está activo"));
	}

	@Test
	void getOdontogramById_IsActiveNull() {
		// Arrange
		odontogram.setIsActive(null);
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.getOdontogramById(1L, 1L));

		assertTrue(exception.getMessage().contains("El odontograma no está activo"));
	}

	@Test
	void getOdontogramById_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.getOdontogramById(1L, 1L));

		assertTrue(exception.getMessage().contains("no pertenece a un paciente de este dentista"));
	}

	// ==================== UPDATE OPERATIONS ====================

	@Test
	void updateOdontogram_Success() {
		// Arrange
		requestDto.setDentitionType("DECIDUOUS");
		requestDto.setTeethData("{\"tooth_51\": {\"status\": \"cavity\"}}");

		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));
		when(odontogramRepository.save(any(Odontogram.class))).thenReturn(odontogram);

		// Act
		OdontogramResponseDto result = odontogramService.updateOdontogram(1L, 1L, requestDto);

		// Assert
		assertNotNull(result);
		verify(odontogramRepository).save(argThat(o ->
				o.getDentitionType().equals("DECIDUOUS") &&
				o.getTeethData().contains("cavity")
		));
	}

	@Test
	void updateOdontogram_NotFound() {
		// Arrange
		when(odontogramRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.updateOdontogram(1L, 1L, requestDto));

		assertTrue(exception.getMessage().contains("No se encontró el odontograma"));
	}

	@Test
	void updateOdontogram_NotActive() {
		// Arrange
		odontogram.setIsActive(false);
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.updateOdontogram(1L, 1L, requestDto));

		assertTrue(exception.getMessage().contains("El odontograma no está activo"));
	}

	@Test
	void updateOdontogram_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.updateOdontogram(1L, 1L, requestDto));

		assertTrue(exception.getMessage().contains("no pertenece a un paciente de este dentista"));
	}

	// ==================== DELETE OPERATIONS ====================

	@Test
	void deleteOdontogram_Success() {
		// Arrange
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));
		when(odontogramRepository.save(any(Odontogram.class))).thenReturn(odontogram);

		// Act
		odontogramService.deleteOdontogram(1L, 1L);

		// Assert
		verify(odontogramRepository).save(argThat(o -> !o.getIsActive()));
	}

	@Test
	void deleteOdontogram_NotFound() {
		// Arrange
		when(odontogramRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.deleteOdontogram(1L, 1L));

		assertTrue(exception.getMessage().contains("No se encontró el odontograma"));
	}

	@Test
	void deleteOdontogram_AlreadyInactive() {
		// Arrange
		odontogram.setIsActive(false);
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.deleteOdontogram(1L, 1L));

		assertTrue(exception.getMessage().contains("El odontograma ya está inactivo"));
	}

	@Test
	void deleteOdontogram_IsActiveNull() {
		// Arrange
		odontogram.setIsActive(null);
		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.deleteOdontogram(1L, 1L));

		assertTrue(exception.getMessage().contains("El odontograma ya está inactivo"));
	}

	@Test
	void deleteOdontogram_PatientDoesNotBelongToDentist() {
		// Arrange
		Dentist otherDentist = new Dentist();
		otherDentist.setId(99L);
		patient.setDentist(otherDentist);

		when(odontogramRepository.findById(1L)).thenReturn(Optional.of(odontogram));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> odontogramService.deleteOdontogram(1L, 1L));

		assertTrue(exception.getMessage().contains("no pertenece a un paciente de este dentista"));
	}

	// ==================== COUNT OPERATIONS ====================

	@Test
	void countOdontogramsByPatient_Success() {
		// Arrange
		when(odontogramRepository.countByPatientIdAndIsActiveTrue(2L)).thenReturn(3L);

		// Act
		long result = odontogramService.countOdontogramsByPatient(2L);

		// Assert
		assertEquals(3L, result);
		verify(odontogramRepository).countByPatientIdAndIsActiveTrue(2L);
	}

	@Test
	void countOdontogramsByPatient_Zero() {
		// Arrange
		when(odontogramRepository.countByPatientIdAndIsActiveTrue(2L)).thenReturn(0L);

		// Act
		long result = odontogramService.countOdontogramsByPatient(2L);

		// Assert
		assertEquals(0L, result);
	}
}
