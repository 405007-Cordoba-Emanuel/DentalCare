package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.request.appointment.AppointmentRequestDto;
import com.dentalCare.be_core.dtos.request.appointment.AppointmentUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentCalendarDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentResponseDto;
import com.dentalCare.be_core.entities.Appointment;
import com.dentalCare.be_core.entities.AppointmentStatus;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.AppointmentRepository;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private DentistRepository dentistRepository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private ModelMapperUtils modelMapperUtils;

	@InjectMocks
	private AppointmentServiceImpl appointmentService;

	private Dentist dentist;
	private Patient patient;
	private Appointment appointment;
	private AppointmentRequestDto requestDto;
	private AppointmentUpdateRequestDto updateRequestDto;
	private UserDetailDto patientUserDto;
	private UserDetailDto dentistUserDto;

	@BeforeEach
	void setUp() {
		// Setup Dentist
		dentist = new Dentist();
		dentist.setId(1L);
		dentist.setUserId(100L);
		dentist.setLicenseNumber("DEN12345");
		dentist.setSpecialty("Orthodontics");

		// Setup Patient
		patient = new Patient();
		patient.setId(1L);
		patient.setUserId(200L);
		patient.setDni("12345678");
		patient.setDentist(dentist);
		patient.setActive(true);

		// Setup Appointment
		appointment = new Appointment();
		appointment.setId(1L);
		appointment.setDentist(dentist);
		appointment.setPatient(patient);
		appointment.setStartDateTime(LocalDateTime.now().plusDays(1));
		appointment.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(1));
		appointment.setStatus(AppointmentStatus.PROGRAMADO);
		appointment.setReason("Routine checkup");
		appointment.setNotes("Patient is nervous");
		appointment.setActive(true);

		// Setup Request DTOs
		requestDto = new AppointmentRequestDto();
		requestDto.setPatientId(1L);
		requestDto.setStartDateTime(LocalDateTime.now().plusDays(1));
		requestDto.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(1));
		requestDto.setReason("Routine checkup");
		requestDto.setNotes("Patient is nervous");

		updateRequestDto = new AppointmentUpdateRequestDto();
		updateRequestDto.setStartDateTime(LocalDateTime.now().plusDays(2));
		updateRequestDto.setEndDateTime(LocalDateTime.now().plusDays(2).plusHours(1));
		updateRequestDto.setReason("Updated reason");
		updateRequestDto.setNotes("Updated notes");

		// Setup User DTOs
		patientUserDto = new UserDetailDto();
		patientUserDto.setFirstName("John");
		patientUserDto.setLastName("Doe");

		dentistUserDto = new UserDetailDto();
		dentistUserDto.setFirstName("Dr. Jane");
		dentistUserDto.setLastName("Smith");
	}

	@Test
	void createAppointmentForDentist_Success() {
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(appointmentRepository.existsTimeConflict(anyLong(), any(), any())).thenReturn(false);
		when(modelMapperUtils.map(any(AppointmentRequestDto.class), eq(Appointment.class))).thenReturn(appointment);
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
		when(userServiceClient.getUserById(200L)).thenReturn(patientUserDto);
		when(userServiceClient.getUserById(100L)).thenReturn(dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		AppointmentResponseDto result = appointmentService.createAppointmentForDentist(1L, requestDto);

		assertNotNull(result);
		verify(appointmentRepository).save(any(Appointment.class));
	}

	@Test
	void createAppointmentForDentist_DentistNotFound() {
		when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("No dentist found with ID: 1", exception.getMessage());
	}

	@Test
	void createAppointmentForDentist_PatientNotFound() {
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("No patient found with ID: 1", exception.getMessage());
	}

	@Test
	void createAppointmentForDentist_PatientNotBelongToDentist() {
		Dentist anotherDentist = new Dentist();
		anotherDentist.setId(2L);
		patient.setDentist(anotherDentist);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("Patient does not belong to this dentist", exception.getMessage());
	}

	@Test
	void createAppointmentForDentist_InactivePatient() {
		patient.setActive(false);

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("The patient is not active", exception.getMessage());
	}

	@Test
	void createAppointmentForDentist_StartAfterEnd() {
		requestDto.setStartDateTime(LocalDateTime.now().plusDays(1));
		requestDto.setEndDateTime(LocalDateTime.now().plusHours(1));

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("Start datetime must be before end datetime", exception.getMessage());
	}

	@Test
	void createAppointmentForDentist_AppointmentInPast() {
		requestDto.setStartDateTime(LocalDateTime.now().minusDays(1));
		requestDto.setEndDateTime(LocalDateTime.now().minusHours(1));

		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("Appointment cannot be scheduled in the past", exception.getMessage());
	}

	@Test
	void createAppointmentForDentist_TimeConflict() {
		when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(appointmentRepository.existsTimeConflict(anyLong(), any(), any())).thenReturn(true);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.createAppointmentForDentist(1L, requestDto));

		assertEquals("There is a time conflict with another appointment", exception.getMessage());
	}

	@Test
	void getAppointmentsByDentistId_Success() {
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findByDentistIdAndActiveTrueOrderByStartDateTimeAsc(1L))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		List<AppointmentResponseDto> result = appointmentService.getAppointmentsByDentistId(1L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getAppointmentsByPatientId_Success() {
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findByPatientIdAndActiveTrueOrderByStartDateTimeAsc(1L))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		List<AppointmentResponseDto> result = appointmentService.getAppointmentsByPatientId(1L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getAppointmentsByDentistIdAndPatientId_Success() {
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findByDentistIdAndPatientIdAndActiveTrueOrderByStartDateTimeAsc(1L, 1L))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		List<AppointmentResponseDto> result = appointmentService.getAppointmentsByDentistIdAndPatientId(1L, 1L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getAppointmentByIdAndDentistId_Success() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		AppointmentResponseDto result = appointmentService.getAppointmentByIdAndDentistId(1L, 1L);

		assertNotNull(result);
	}

	@Test
	void getAppointmentByIdAndDentistId_NotFound() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.getAppointmentByIdAndDentistId(1L, 1L));

		assertEquals("No appointment found with ID: 1", exception.getMessage());
	}

	@Test
	void updateAppointment_Success() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));
		when(appointmentRepository.existsTimeConflictExcluding(anyLong(), anyLong(), any(), any()))
				.thenReturn(false);
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		AppointmentResponseDto result = appointmentService.updateAppointment(1L, 1L, updateRequestDto);

		assertNotNull(result);
		verify(appointmentRepository).save(any(Appointment.class));
	}

	@Test
	void updateAppointment_StartAfterEnd() {
		updateRequestDto.setStartDateTime(LocalDateTime.now().plusDays(2));
		updateRequestDto.setEndDateTime(LocalDateTime.now().plusDays(1));

		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.updateAppointment(1L, 1L, updateRequestDto));

		assertEquals("Start datetime must be before end datetime", exception.getMessage());
	}

	@Test
	void updateAppointment_TimeConflict() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));
		when(appointmentRepository.existsTimeConflictExcluding(anyLong(), anyLong(), any(), any()))
				.thenReturn(true);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.updateAppointment(1L, 1L, updateRequestDto));

		assertEquals("There is a time conflict with another appointment", exception.getMessage());
	}

	@Test
	void updateAppointmentStatus_Success() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		AppointmentResponseDto result = appointmentService.updateAppointmentStatus(1L, 1L, AppointmentStatus.COMPLETADO);

		assertNotNull(result);
		verify(appointmentRepository).save(any(Appointment.class));
	}

	@Test
	void cancelAppointment_Success() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

		appointmentService.cancelAppointment(1L, 1L);

		verify(appointmentRepository).save(argThat(app ->
				app.getStatus() == AppointmentStatus.CANCELADO && !app.getActive()
		));
	}

	@Test
	void getMonthlyAppointments_Success() {
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findByDentistIdAndMonth(1L, 2024, 12))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);

		List<AppointmentCalendarDto> result = appointmentService.getMonthlyAppointments(1L, 2024, 12);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getWeeklyAppointments_Success() {
		LocalDate startDate = LocalDate.now();
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findByDentistIdAndDateRange(anyLong(), any(), any()))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);

		List<AppointmentCalendarDto> result = appointmentService.getWeeklyAppointments(1L, startDate);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getDailyAppointments_Success() {
		LocalDate date = LocalDate.now();
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findByDentistIdAndDate(1L, date))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);

		List<AppointmentCalendarDto> result = appointmentService.getDailyAppointments(1L, date);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getUpcomingAppointmentsByPatientId_Success() {
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findUpcomingByPatientId(anyLong(), any()))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		List<AppointmentResponseDto> result = appointmentService.getUpcomingAppointmentsByPatientId(1L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getPastAppointmentsByPatientId_Success() {
		List<Appointment> appointments = Arrays.asList(appointment);
		when(appointmentRepository.findPastByPatientId(anyLong(), any()))
				.thenReturn(appointments);
		when(userServiceClient.getUserById(anyLong())).thenReturn(patientUserDto, dentistUserDto);
		when(modelMapperUtils.map(any(Appointment.class), eq(AppointmentResponseDto.class)))
				.thenReturn(new AppointmentResponseDto());

		List<AppointmentResponseDto> result = appointmentService.getPastAppointmentsByPatientId(1L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void hasTimeConflict_ReturnsTrue() {
		when(appointmentRepository.existsTimeConflict(anyLong(), any(), any())).thenReturn(true);

		boolean result = appointmentService.hasTimeConflict(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

		assertTrue(result);
	}

	@Test
	void hasTimeConflict_ReturnsFalse() {
		when(appointmentRepository.existsTimeConflict(anyLong(), any(), any())).thenReturn(false);

		boolean result = appointmentService.hasTimeConflict(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

		assertFalse(result);
	}

	@Test
	void countAppointmentsByDentistIdAndStatus_Success() {
		when(appointmentRepository.countByDentistIdAndStatusAndActiveTrue(1L, AppointmentStatus.PROGRAMADO))
				.thenReturn(5L);

		long result = appointmentService.countAppointmentsByDentistIdAndStatus(1L, AppointmentStatus.PROGRAMADO);

		assertEquals(5L, result);
	}

	@Test
	void countAppointmentsByPatientIdAndStatus_Success() {
		when(appointmentRepository.countByPatientIdAndStatusAndActiveTrue(1L, AppointmentStatus.COMPLETADO))
				.thenReturn(3L);

		long result = appointmentService.countAppointmentsByPatientIdAndStatus(1L, AppointmentStatus.COMPLETADO);

		assertEquals(3L, result);
	}

	@Test
	void getAppointmentEntityByIdAndDentistId_Success() {
		when(appointmentRepository.findByIdAndDentistIdAndActiveTrue(1L, 1L))
				.thenReturn(Optional.of(appointment));

		Appointment result = appointmentService.getAppointmentEntityByIdAndDentistId(1L, 1L);

		assertNotNull(result);
		assertEquals(appointment, result);
	}
}