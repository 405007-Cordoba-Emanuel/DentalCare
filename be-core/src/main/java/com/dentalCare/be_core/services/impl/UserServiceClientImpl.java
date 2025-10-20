package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserServiceClientImpl implements UserServiceClient {

    @Value("${users.service.url:http://localhost:8081}")
    private String usersServiceUrl;

    private final RestTemplate restTemplate;

    public UserServiceClientImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public UserDetailDto getUserById(Long userId) {
        try {
            String url = usersServiceUrl + "/public/users/" + userId;
            log.info("Calling users MS: {}", url);
            UserDetailDto user = restTemplate.getForObject(url, UserDetailDto.class);
            return user;
        } catch (Exception e) {
            log.error("Error calling users microservice for userId: {}", userId, e);
            throw new RuntimeException("Error getting user information from users microservice", e);
        }
    }
}
