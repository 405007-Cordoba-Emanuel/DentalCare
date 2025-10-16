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
import com.dentalCare.be_core.services.AppointmentService;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ModelMapperUtils modelMapperUtils;

    @Override
    public AppointmentResponseDto createAppointmentForDentist(Long dentistId, AppointmentRequestDto appointmentRequestDto) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No dentist found with ID: " + dentistId));

        Patient patient = patientRepository.findById(appointmentRequestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No patient found with ID: " + appointmentRequestDto.getPatientId()));

        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("Patient does not belong to this dentist");
        }

        if (patient.getActive() == null || !patient.getActive()) {
            throw new IllegalArgumentException("The patient is not active");
        }

        // Validar que la fecha de inicio sea anterior a la fecha de fin
        if (appointmentRequestDto.getStartDateTime().isAfter(appointmentRequestDto.getEndDateTime())) {
            throw new IllegalArgumentException("Start datetime must be before end datetime");
        }

        // Validar que la fecha de inicio sea en el futuro
        if (appointmentRequestDto.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled in the past");
        }

        // Verificar conflicto de horario
        if (hasTimeConflict(dentistId, appointmentRequestDto.getStartDateTime(), appointmentRequestDto.getEndDateTime())) {
            throw new IllegalArgumentException("There is a time conflict with another appointment");
        }

        Appointment appointment = modelMapperUtils.map(appointmentRequestDto, Appointment.class);
        appointment.setId(null);
        appointment.setDentist(dentist);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setActive(true);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToResponseDto(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByDentistId(Long dentistId) {
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndActiveTrueOrderByStartDateTimeAsc(dentistId);
        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndActiveTrueOrderByStartDateTimeAsc(patientId);
        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByDentistIdAndPatientId(Long dentistId, Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndPatientIdAndActiveTrueOrderByStartDateTimeAsc(dentistId, patientId);
        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentByIdAndDentistId(Long appointmentId, Long dentistId) {
        Appointment appointment = appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));
        return mapToResponseDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentByIdAndPatientId(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findByIdAndPatientIdAndActiveTrue(appointmentId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));
        return mapToResponseDto(appointment);
    }

    @Override
    public AppointmentResponseDto updateAppointment(Long appointmentId, Long dentistId, AppointmentUpdateRequestDto appointmentUpdateRequestDto) {
        Appointment appointment = appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));

        // Validar que la fecha de inicio sea anterior a la fecha de fin
        if (appointmentUpdateRequestDto.getStartDateTime().isAfter(appointmentUpdateRequestDto.getEndDateTime())) {
            throw new IllegalArgumentException("Start datetime must be before end datetime");
        }

        // Verificar conflicto de horario excluyendo el turno actual
        if (hasTimeConflictExcluding(dentistId, appointmentId, appointmentUpdateRequestDto.getStartDateTime(), appointmentUpdateRequestDto.getEndDateTime())) {
            throw new IllegalArgumentException("There is a time conflict with another appointment");
        }

        appointment.setStartDateTime(appointmentUpdateRequestDto.getStartDateTime());
        appointment.setEndDateTime(appointmentUpdateRequestDto.getEndDateTime());
        appointment.setReason(appointmentUpdateRequestDto.getReason());
        appointment.setNotes(appointmentUpdateRequestDto.getNotes());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToResponseDto(updatedAppointment);
    }

    @Override
    public AppointmentResponseDto updateAppointmentStatus(Long appointmentId, Long dentistId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));

        appointment.setStatus(status);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToResponseDto(updatedAppointment);
    }

    @Override
    public void cancelAppointment(Long appointmentId, Long dentistId) {
        Appointment appointment = appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setActive(false);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarDto> getMonthlyAppointments(Long dentistId, int year, int month) {
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndMonth(dentistId, year, month);
        return appointments.stream()
                .map(this::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarDto> getWeeklyAppointments(Long dentistId, LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = startDate.plusDays(7).atStartOfDay();
        
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndDateRange(dentistId, startDateTime, endDateTime);
        return appointments.stream()
                .map(this::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarDto> getDailyAppointments(Long dentistId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndDate(dentistId, date);
        return appointments.stream()
                .map(this::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getUpcomingAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findUpcomingByPatientId(patientId, LocalDateTime.now());
        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTimeConflict(Long dentistId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentRepository.existsTimeConflict(dentistId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTimeConflictExcluding(Long dentistId, Long excludeAppointmentId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentRepository.existsTimeConflictExcluding(dentistId, excludeAppointmentId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAppointmentsByDentistIdAndStatus(Long dentistId, AppointmentStatus status) {
        return appointmentRepository.countByDentistIdAndStatusAndActiveTrue(dentistId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAppointmentsByPatientIdAndStatus(Long patientId, AppointmentStatus status) {
        return appointmentRepository.countByPatientIdAndStatusAndActiveTrue(patientId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentEntityByIdAndDentistId(Long appointmentId, Long dentistId) {
        return appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));
    }

    private AppointmentResponseDto mapToResponseDto(Appointment appointment) {
        AppointmentResponseDto responseDto = modelMapperUtils.map(appointment, AppointmentResponseDto.class);

        UserDetailDto patientUser = userServiceClient.getUserById(appointment.getPatient().getUserId());
        UserDetailDto dentistUser = userServiceClient.getUserById(appointment.getDentist().getUserId());

        responseDto.setPatientId(appointment.getPatient().getId());
        responseDto.setPatientName(patientUser.getFirstName() + " " + patientUser.getLastName());
        responseDto.setPatientDni(appointment.getPatient().getDni());
        responseDto.setDentistId(appointment.getDentist().getId());
        responseDto.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        responseDto.setDentistLicenseNumber(appointment.getDentist().getLicenseNumber());
        responseDto.setDentistSpecialty(appointment.getDentist().getSpecialty());
        responseDto.setDurationMinutes(appointment.getDurationMinutes());

        return responseDto;
    }

    private AppointmentCalendarDto mapToCalendarDto(Appointment appointment) {
        AppointmentCalendarDto calendarDto = new AppointmentCalendarDto();

        UserDetailDto patientUser = userServiceClient.getUserById(appointment.getPatient().getUserId());
        UserDetailDto dentistUser = userServiceClient.getUserById(appointment.getDentist().getUserId());

        calendarDto.setId(appointment.getId());
        calendarDto.setPatientId(appointment.getPatient().getId());
        calendarDto.setPatientName(patientUser.getFirstName() + " " + patientUser.getLastName());
        calendarDto.setPatientDni(appointment.getPatient().getDni());
        calendarDto.setDentistId(appointment.getDentist().getId());
        calendarDto.setDentistName(dentistUser.getFirstName() + " " + dentistUser.getLastName());
        calendarDto.setDate(appointment.getStartDateTime().toLocalDate());
        calendarDto.setStartTime(appointment.getStartDateTime().toLocalTime());
        calendarDto.setEndTime(appointment.getEndDateTime().toLocalTime());
        calendarDto.setDurationMinutes(appointment.getDurationMinutes());
        calendarDto.setStatus(appointment.getStatus());
        calendarDto.setReason(appointment.getReason());
        calendarDto.setNotes(appointment.getNotes());
        calendarDto.setActive(appointment.getActive());

        return calendarDto;
    }
}
