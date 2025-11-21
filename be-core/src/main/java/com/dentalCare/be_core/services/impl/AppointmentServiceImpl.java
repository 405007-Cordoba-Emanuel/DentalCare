package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.external.EmailRequestDto;
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
import com.dentalCare.be_core.services.EmailServiceClient;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private EmailServiceClient emailServiceClient;

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
        appointment.setStatus(AppointmentStatus.PROGRAMADO);
        appointment.setActive(true);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Enviar notificaciones por email
        sendAppointmentCreatedEmails(savedAppointment, dentist, patient);
        
        return mapToResponseDto(savedAppointment);
    }

    private void sendAppointmentCreatedEmails(Appointment appointment, Dentist dentist, Patient patient) {
        try {
            UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
            UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
            
            // Formatear fecha y hora
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "ES"));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            String appointmentDate = appointment.getStartDateTime().format(dateFormatter);
            String startTime = appointment.getStartDateTime().format(timeFormatter);
            String endTime = appointment.getEndDateTime().format(timeFormatter);
            long duration = java.time.Duration.between(appointment.getStartDateTime(), appointment.getEndDateTime()).toMinutes();
            
            // Email para el paciente
            Map<String, Object> patientVariables = new HashMap<>();
            patientVariables.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            patientVariables.put("dentistName", "Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName());
            patientVariables.put("dentistSpecialty", dentist.getSpecialty());
            patientVariables.put("dentistLicense", dentist.getLicenseNumber());
            patientVariables.put("appointmentDate", appointmentDate);
            patientVariables.put("startTime", startTime);
            patientVariables.put("endTime", endTime);
            patientVariables.put("duration", duration);
            patientVariables.put("reason", appointment.getReason() != null ? appointment.getReason() : "");
            patientVariables.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
            
            EmailRequestDto patientEmail = EmailRequestDto.builder()
                    .to(List.of(patientUser.getEmail()))
                    .subject("Confirmación de Cita - DentalCare")
                    .emailType("APPOINTMENT_CREATED_PATIENT")
                    .variables(patientVariables)
                    .build();
            
            emailServiceClient.sendEmail(patientEmail);
            log.info("Appointment confirmation email sent to patient: {}", patientUser.getEmail());
            
            // Email para el dentista
            Map<String, Object> dentistVariables = new HashMap<>();
            dentistVariables.put("dentistName", "Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName());
            dentistVariables.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            dentistVariables.put("patientDni", patient.getDni());
            dentistVariables.put("appointmentDate", appointmentDate);
            dentistVariables.put("startTime", startTime);
            dentistVariables.put("endTime", endTime);
            dentistVariables.put("duration", duration);
            dentistVariables.put("reason", appointment.getReason() != null ? appointment.getReason() : "");
            dentistVariables.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
            
            EmailRequestDto dentistEmail = EmailRequestDto.builder()
                    .to(List.of(dentistUser.getEmail()))
                    .subject("Nueva Cita Agendada - DentalCare")
                    .emailType("APPOINTMENT_CREATED_DENTIST")
                    .variables(dentistVariables)
                    .build();
            
            emailServiceClient.sendEmail(dentistEmail);
            log.info("Appointment notification email sent to dentist: {}", dentistUser.getEmail());
            
        } catch (Exception e) {
            log.error("Error sending appointment emails: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    private void sendAppointmentUpdatedEmails(Appointment appointment, LocalDateTime originalStartDateTime, LocalDateTime originalEndDateTime) {
        try {
            Dentist dentist = appointment.getDentist();
            Patient patient = appointment.getPatient();
            
            UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
            UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
            
            // Formatear fecha y hora
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "ES"));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            // Datos originales
            String originalAppointmentDate = originalStartDateTime.format(dateFormatter);
            String originalStartTime = originalStartDateTime.format(timeFormatter);
            String originalEndTime = originalEndDateTime.format(timeFormatter);
            
            // Datos nuevos
            String newAppointmentDate = appointment.getStartDateTime().format(dateFormatter);
            String newStartTime = appointment.getStartDateTime().format(timeFormatter);
            String newEndTime = appointment.getEndDateTime().format(timeFormatter);
            long duration = java.time.Duration.between(appointment.getStartDateTime(), appointment.getEndDateTime()).toMinutes();
            
            // Email para el paciente
            Map<String, Object> patientVariables = new HashMap<>();
            patientVariables.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            patientVariables.put("dentistName", "Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName());
            patientVariables.put("dentistSpecialty", dentist.getSpecialty());
            patientVariables.put("dentistLicense", dentist.getLicenseNumber());
            patientVariables.put("originalAppointmentDate", originalAppointmentDate);
            patientVariables.put("originalStartTime", originalStartTime);
            patientVariables.put("originalEndTime", originalEndTime);
            patientVariables.put("newAppointmentDate", newAppointmentDate);
            patientVariables.put("newStartTime", newStartTime);
            patientVariables.put("newEndTime", newEndTime);
            patientVariables.put("duration", duration);
            patientVariables.put("reason", appointment.getReason() != null ? appointment.getReason() : "");
            patientVariables.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
            
            EmailRequestDto patientEmail = EmailRequestDto.builder()
                    .to(List.of(patientUser.getEmail()))
                    .subject("Cambio en tu Cita - DentalCare")
                    .emailType("APPOINTMENT_UPDATED_PATIENT")
                    .variables(patientVariables)
                    .build();
            
            emailServiceClient.sendEmail(patientEmail);
            log.info("Appointment update email sent to patient: {}", patientUser.getEmail());
            
            // Email para el dentista
            Map<String, Object> dentistVariables = new HashMap<>();
            dentistVariables.put("dentistName", "Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName());
            dentistVariables.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            dentistVariables.put("patientDni", patient.getDni());
            dentistVariables.put("originalAppointmentDate", originalAppointmentDate);
            dentistVariables.put("originalStartTime", originalStartTime);
            dentistVariables.put("originalEndTime", originalEndTime);
            dentistVariables.put("newAppointmentDate", newAppointmentDate);
            dentistVariables.put("newStartTime", newStartTime);
            dentistVariables.put("newEndTime", newEndTime);
            dentistVariables.put("duration", duration);
            dentistVariables.put("reason", appointment.getReason() != null ? appointment.getReason() : "");
            dentistVariables.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
            
            EmailRequestDto dentistEmail = EmailRequestDto.builder()
                    .to(List.of(dentistUser.getEmail()))
                    .subject("Cita Modificada - DentalCare")
                    .emailType("APPOINTMENT_UPDATED_DENTIST")
                    .variables(dentistVariables)
                    .build();
            
            emailServiceClient.sendEmail(dentistEmail);
            log.info("Appointment update email sent to dentist: {}", dentistUser.getEmail());
            
        } catch (Exception e) {
            log.error("Error sending appointment update emails: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    private void sendAppointmentStatusChangedEmails(Appointment appointment, AppointmentStatus originalStatus) {
        try {
            Dentist dentist = appointment.getDentist();
            Patient patient = appointment.getPatient();
            
            UserDetailDto patientUser = userServiceClient.getUserById(patient.getUserId());
            UserDetailDto dentistUser = userServiceClient.getUserById(dentist.getUserId());
            
            // Formatear fecha y hora
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "ES"));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            String appointmentDate = appointment.getStartDateTime().format(dateFormatter);
            String startTime = appointment.getStartDateTime().format(timeFormatter);
            String endTime = appointment.getEndDateTime().format(timeFormatter);
            long duration = java.time.Duration.between(appointment.getStartDateTime(), appointment.getEndDateTime()).toMinutes();
            
            // Traducir estados al español
            String originalStatusSpanish = translateStatus(originalStatus);
            String newStatusSpanish = translateStatus(appointment.getStatus());
            
            // Email para el paciente
            Map<String, Object> patientVariables = new HashMap<>();
            patientVariables.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            patientVariables.put("dentistName", "Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName());
            patientVariables.put("dentistSpecialty", dentist.getSpecialty());
            patientVariables.put("dentistLicense", dentist.getLicenseNumber());
            patientVariables.put("appointmentDate", appointmentDate);
            patientVariables.put("startTime", startTime);
            patientVariables.put("endTime", endTime);
            patientVariables.put("duration", duration);
            patientVariables.put("originalStatus", originalStatusSpanish);
            patientVariables.put("newStatus", newStatusSpanish);
            patientVariables.put("reason", appointment.getReason() != null ? appointment.getReason() : "");
            patientVariables.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
            
            EmailRequestDto patientEmail = EmailRequestDto.builder()
                    .to(List.of(patientUser.getEmail()))
                    .subject("Cambio de Estado en tu Cita - DentalCare")
                    .emailType("APPOINTMENT_STATUS_CHANGED_PATIENT")
                    .variables(patientVariables)
                    .build();
            
            emailServiceClient.sendEmail(patientEmail);
            log.info("Appointment status change email sent to patient: {}", patientUser.getEmail());
            
            // Email para el dentista
            Map<String, Object> dentistVariables = new HashMap<>();
            dentistVariables.put("dentistName", "Dr./Dra. " + dentistUser.getFirstName() + " " + dentistUser.getLastName());
            dentistVariables.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            dentistVariables.put("patientDni", patient.getDni());
            dentistVariables.put("appointmentDate", appointmentDate);
            dentistVariables.put("startTime", startTime);
            dentistVariables.put("endTime", endTime);
            dentistVariables.put("duration", duration);
            dentistVariables.put("originalStatus", originalStatusSpanish);
            dentistVariables.put("newStatus", newStatusSpanish);
            dentistVariables.put("reason", appointment.getReason() != null ? appointment.getReason() : "");
            dentistVariables.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
            
            EmailRequestDto dentistEmail = EmailRequestDto.builder()
                    .to(List.of(dentistUser.getEmail()))
                    .subject("Cambio de Estado de Cita - DentalCare")
                    .emailType("APPOINTMENT_STATUS_CHANGED_DENTIST")
                    .variables(dentistVariables)
                    .build();
            
            emailServiceClient.sendEmail(dentistEmail);
            log.info("Appointment status change email sent to dentist: {}", dentistUser.getEmail());
            
        } catch (Exception e) {
            log.error("Error sending appointment status change emails: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    private String translateStatus(AppointmentStatus status) {
        switch (status) {
            case PROGRAMADO:
                return "Programado";
            case CONFIRMADO:
                return "Confirmado";
            case COMPLETADO:
                return "Completado";
            case CANCELADO:
                return "Cancelado";
            case AUSENTE:
                return "Ausente";
            default:
                return status.name();
        }
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

        // Guardar valores originales para detectar cambios
        LocalDateTime originalStartDateTime = appointment.getStartDateTime();
        LocalDateTime originalEndDateTime = appointment.getEndDateTime();
        
        // Detectar si cambió la fecha u hora
        boolean dateOrTimeChanged = !originalStartDateTime.equals(appointmentUpdateRequestDto.getStartDateTime()) 
                                    || !originalEndDateTime.equals(appointmentUpdateRequestDto.getEndDateTime());

        appointment.setStartDateTime(appointmentUpdateRequestDto.getStartDateTime());
        appointment.setEndDateTime(appointmentUpdateRequestDto.getEndDateTime());
        appointment.setReason(appointmentUpdateRequestDto.getReason());
        appointment.setNotes(appointmentUpdateRequestDto.getNotes());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        // Enviar notificaciones por email si cambió la fecha u hora
        if (dateOrTimeChanged) {
            sendAppointmentUpdatedEmails(updatedAppointment, originalStartDateTime, originalEndDateTime);
        }
        
        return mapToResponseDto(updatedAppointment);
    }

    @Override
    public AppointmentResponseDto updateAppointmentStatus(Long appointmentId, Long dentistId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));

        // Guardar estado original
        AppointmentStatus originalStatus = appointment.getStatus();
        
        appointment.setStatus(status);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        // Enviar notificaciones por email si cambió el estado
        if (!originalStatus.equals(status)) {
            sendAppointmentStatusChangedEmails(updatedAppointment, originalStatus);
        }
        
        return mapToResponseDto(updatedAppointment);
    }

    @Override
    public void cancelAppointment(Long appointmentId, Long dentistId) {
        Appointment appointment = appointmentRepository.findByIdAndDentistIdAndActiveTrue(appointmentId, dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No appointment found with ID: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELADO);
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
    public List<AppointmentResponseDto> getPastAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findPastByPatientId(patientId, LocalDateTime.now());
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

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByDentistIdExcludingCancelled(Long dentistId) {
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndActiveTrueAndStatusNotCancelled(dentistId);
        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByPatientIdExcludingCancelled(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndActiveTrueAndStatusNotCancelled(patientId);
        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarDto> getTwoYearAppointmentsByDentistId(Long dentistId) {
        // Calcular rango de fechas: 1 año atrás + 1 año adelante desde hoy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusYears(1);
        LocalDateTime endDate = now.plusYears(1);
        
        List<Appointment> appointments = appointmentRepository.findByDentistIdAndTwoYearRange(dentistId, startDate, endDate);
        return appointments.stream()
                .map(this::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarDto> getTwoYearAppointmentsByPatientId(Long patientId) {
        // Calcular rango de fechas: 1 año atrás + 1 año adelante desde hoy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusYears(1);
        LocalDateTime endDate = now.plusYears(1);
        
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndTwoYearRange(patientId, startDate, endDate);
        return appointments.stream()
                .map(this::mapToCalendarDto)
                .collect(Collectors.toList());
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
