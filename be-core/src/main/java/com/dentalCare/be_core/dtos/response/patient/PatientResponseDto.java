package com.dentalCare.be_core.dtos.response.patient;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String dni;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String birthDate;
    private String phone;
    private String email;
    private String address;
    private Boolean active;

}