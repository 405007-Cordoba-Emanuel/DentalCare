package com.dentalCare.be_core.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableUserDto {
    
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String picture;
    private String role;
    private boolean isActive;
}
