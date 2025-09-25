package com.dentalCare.be_core.dtos.response.dentist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DentistPatientsResponseDto {
    
    private Long dentistId;
    private String dentistName;
    private String licenseNumber;
    private String specialty;
    private List<PatientSummaryDto> patients;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientSummaryDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String dni;
        private String email;
        private String phone;
        private Boolean active;
    }
}
