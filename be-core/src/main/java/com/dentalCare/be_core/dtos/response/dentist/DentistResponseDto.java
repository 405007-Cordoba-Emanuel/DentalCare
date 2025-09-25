package com.dentalCare.be_core.dtos.response.dentist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DentistResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String specialty;
    private String phone;
    private String email;
    private String address;
    private Boolean active;

}