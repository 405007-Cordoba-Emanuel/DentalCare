package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.appointment.AppointmentRequestDto;
import com.dentalCare.be_core.dtos.request.appointment.AppointmentUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentCalendarDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentResponseDto;
import com.dentalCare.be_core.entities.Appointment;
import com.dentalCare.be_core.entities.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface que define los servicios disponibles para la gestión de turnos/citas
 */
public interface AppointmentService {

    AppointmentResponseDto createAppointmentForDentist(Long dentistId, AppointmentRequestDto appointmentRequestDto);

    List<AppointmentResponseDto> getAppointmentsByDentistId(Long dentistId);

    List<AppointmentResponseDto> getAppointmentsByPatientId(Long patientId);

    List<AppointmentResponseDto> getAppointmentsByDentistIdAndPatientId(Long dentistId, Long patientId);

    AppointmentResponseDto getAppointmentByIdAndDentistId(Long appointmentId, Long dentistId);

    AppointmentResponseDto getAppointmentByIdAndPatientId(Long appointmentId, Long patientId);

    AppointmentResponseDto updateAppointment(Long appointmentId, Long dentistId, AppointmentUpdateRequestDto appointmentUpdateRequestDto);

    AppointmentResponseDto updateAppointmentStatus(Long appointmentId, Long dentistId, AppointmentStatus status);

    void cancelAppointment(Long appointmentId, Long dentistId);

    List<AppointmentCalendarDto> getMonthlyAppointments(Long dentistId, int year, int month);

    List<AppointmentCalendarDto> getWeeklyAppointments(Long dentistId, LocalDate startDate);

    List<AppointmentCalendarDto> getDailyAppointments(Long dentistId, LocalDate date);

    List<AppointmentResponseDto> getUpcomingAppointmentsByPatientId(Long patientId);

    boolean hasTimeConflict(Long dentistId, LocalDateTime startTime, LocalDateTime endTime);

    boolean hasTimeConflictExcluding(Long dentistId, Long excludeAppointmentId, LocalDateTime startTime, LocalDateTime endTime);

    long countAppointmentsByDentistIdAndStatus(Long dentistId, AppointmentStatus status);

    long countAppointmentsByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    Appointment getAppointmentEntityByIdAndDentistId(Long appointmentId, Long dentistId);
}
