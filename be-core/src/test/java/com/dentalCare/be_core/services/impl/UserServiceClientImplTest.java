package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.UserDetailDto;
import com.dentalCare.be_core.dtos.external.UserDetailResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceClientImplTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private UserServiceClientImpl userServiceClient;

	private static final String USERS_SERVICE_URL = "http://localhost:8081";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(userServiceClient, "usersServiceUrl", USERS_SERVICE_URL);
		ReflectionTestUtils.setField(userServiceClient, "restTemplate", restTemplate);
	}

	@Test
	void getUserById_Success() {
		// Arrange
		Long userId = 1L;
		UserDetailDto expectedUser = createUserDetailDto(userId, "John", "Doe", "PATIENT");
		String expectedUrl = USERS_SERVICE_URL + "/public/users/" + userId;

		when(restTemplate.getForObject(expectedUrl, UserDetailDto.class))
				.thenReturn(expectedUser);

		// Act
		UserDetailDto result = userServiceClient.getUserById(userId);

		// Assert
		assertNotNull(result);
		assertEquals(userId, result.getUserId());
		assertEquals("John", result.getFirstName());
		assertEquals("Doe", result.getLastName());
		verify(restTemplate, times(1)).getForObject(expectedUrl, UserDetailDto.class);
	}

	@Test
	void getUserById_ThrowsException() {
		// Arrange
		Long userId = 1L;
		String expectedUrl = USERS_SERVICE_URL + "/public/users/" + userId;

		when(restTemplate.getForObject(expectedUrl, UserDetailDto.class))
				.thenThrow(new RuntimeException("Connection error"));

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> userServiceClient.getUserById(userId));

		assertTrue(exception.getMessage().contains("Error getting user information"));
		verify(restTemplate, times(1)).getForObject(expectedUrl, UserDetailDto.class);
	}

	@Test
	void getUsersByRole_Success() {
		// Arrange
		String role = "DENTIST";
		List<UserDetailResponseDto> mockResponse = Arrays.asList(
				createUserDetailResponseDto(1L, "Jane", "Smith", role),
				createUserDetailResponseDto(2L, "Bob", "Johnson", role)
		);
		String expectedUrl = USERS_SERVICE_URL + "/public/users/role/" + role;

		when(restTemplate.exchange(
				eq(expectedUrl),
				eq(HttpMethod.GET),
				isNull(),
				any(ParameterizedTypeReference.class)
		)).thenReturn(ResponseEntity.ok(mockResponse));

		// Act
		List<UserDetailDto> result = userServiceClient.getUsersByRole(role);

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Jane", result.get(0).getFirstName());
		assertEquals("Bob", result.get(1).getFirstName());
		assertEquals(role, result.get(0).getRole());
		verify(restTemplate, times(1)).exchange(
				eq(expectedUrl),
				eq(HttpMethod.GET),
				isNull(),
				any(ParameterizedTypeReference.class)
		);
	}

	@Test
	void getUsersByRole_EmptyList() {
		// Arrange
		String role = "ADMIN";
		String expectedUrl = USERS_SERVICE_URL + "/public/users/role/" + role;

		when(restTemplate.exchange(
				eq(expectedUrl),
				eq(HttpMethod.GET),
				isNull(),
				any(ParameterizedTypeReference.class)
		)).thenReturn(ResponseEntity.ok(null));

		// Act
		List<UserDetailDto> result = userServiceClient.getUsersByRole(role);

		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void getUsersByRole_ThrowsException() {
		// Arrange
		String role = "DENTIST";
		String expectedUrl = USERS_SERVICE_URL + "/public/users/role/" + role;

		when(restTemplate.exchange(
				eq(expectedUrl),
				eq(HttpMethod.GET),
				isNull(),
				any(ParameterizedTypeReference.class)
		)).thenThrow(new RuntimeException("Service unavailable"));

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> userServiceClient.getUsersByRole(role));

		assertTrue(exception.getMessage().contains("Error getting users by role"));
	}

	// Helper methods
	private UserDetailDto createUserDetailDto(Long id, String firstName, String lastName, String role) {
		UserDetailDto dto = new UserDetailDto();
		dto.setUserId(id);
		dto.setFirstName(firstName);
		dto.setLastName(lastName);
		dto.setEmail(firstName.toLowerCase() + "@example.com");
		dto.setPhone("123456789");
		dto.setRole(role);
		dto.setIsActive(true);
		return dto;
	}

	private UserDetailResponseDto createUserDetailResponseDto(Long id, String firstName, String lastName, String role) {
		UserDetailResponseDto dto = new UserDetailResponseDto();
		dto.setId(id);
		dto.setFirstName(firstName);
		dto.setLastName(lastName);
		dto.setEmail(firstName.toLowerCase() + "@example.com");
		dto.setPhone("123456789");
		dto.setAddress("123 Main St");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setRole(role);
		dto.setIsActive(true);
		dto.setPicture("picture.jpg");
		return dto;
	}
}