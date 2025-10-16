package com.dentalCare.be_core.dtos.response.appointment;

import com.dentalCare.be_core.entities.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCalendarDto {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientDni;
    private Long dentistId;
    private String dentistName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    
    private Integer durationMinutes;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    private Boolean active;
}
