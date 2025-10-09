package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.external.UserDetailDto;

public interface UserServiceClient {

    UserDetailDto getUserById(Long userId);
}
