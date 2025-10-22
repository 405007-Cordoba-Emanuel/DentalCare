package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.external.UserDetailDto;

import java.util.List;

public interface UserServiceClient {

    UserDetailDto getUserById(Long userId);
    
    List<UserDetailDto> getUsersByRole(String role);
}
