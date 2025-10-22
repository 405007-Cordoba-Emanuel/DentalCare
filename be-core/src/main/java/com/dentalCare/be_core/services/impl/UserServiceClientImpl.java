package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.external.UserDetailResponseDto;
import com.dentalCare.be_core.services.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<UserDetailDto> getUsersByRole(String role) {
        try {
            String url = usersServiceUrl + "/public/users/role/" + role;
            log.info("Calling users MS for role {}: {}", role, url);
            List<UserDetailResponseDto> responseUsers = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserDetailResponseDto>>() {}
            ).getBody();
            
            // Mapear de UserDetailResponseDto a UserDetailDto
            if (responseUsers != null) {
                return responseUsers.stream()
                    .map(this::mapToUserDetailDto)
                    .collect(Collectors.toList());
            }
            
            return List.of();
        } catch (Exception e) {
            log.error("Error calling users microservice for role: {}", role, e);
            throw new RuntimeException("Error getting users by role from users microservice", e);
        }
    }

    private UserDetailDto mapToUserDetailDto(UserDetailResponseDto responseDto) {
        UserDetailDto dto = new UserDetailDto();
        dto.setUserId(responseDto.getId());
        dto.setFirstName(responseDto.getFirstName());
        dto.setLastName(responseDto.getLastName());
        dto.setEmail(responseDto.getEmail());
        dto.setPhone(responseDto.getPhone());
        dto.setAddress(responseDto.getAddress());
        dto.setBirthDate(responseDto.getBirthDate());
        dto.setRole(responseDto.getRole());
        dto.setIsActive(responseDto.getIsActive());
        dto.setPicture(responseDto.getPicture());
        return dto;
    }
}
