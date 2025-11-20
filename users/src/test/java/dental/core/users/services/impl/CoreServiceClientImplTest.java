package dental.core.users.services.impl;

import dental.core.users.dto.CreateDentistFromUserRequest;
import dental.core.users.dto.CreatePatientFromUserRequest;
import dental.core.users.dto.DentistResponse;
import dental.core.users.dto.PatientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreServiceClientImplTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private CoreServiceClientImpl coreServiceClient;

	private static final String CORE_SERVICE_URL = "http://localhost:8082";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(coreServiceClient, "coreServiceUrl", CORE_SERVICE_URL);
	}

	@Test
	void createDentistFromUser_Success() {
		// Given
		CreateDentistFromUserRequest request = new CreateDentistFromUserRequest();
		request.setUserId(1L);
		request.setLicenseNumber("LIC123");
		request.setSpecialty("Orthodontics");

		DentistResponse expectedResponse = new DentistResponse();
		expectedResponse.setId(100L);
		expectedResponse.setUserId(1L);
		expectedResponse.setLicenseNumber("LIC123");
		expectedResponse.setSpecialty("Orthodontics");

		when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				any(ParameterizedTypeReference.class)
		)).thenReturn(ResponseEntity.ok(expectedResponse));

		// When
		DentistResponse result = coreServiceClient.createDentistFromUser(request);

		// Then
		assertNotNull(result);
		assertEquals(100L, result.getId());
		assertEquals(1L, result.getUserId());
		assertEquals("LIC123", result.getLicenseNumber());
		assertEquals("Orthodontics", result.getSpecialty());

		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(restTemplate).exchange(
				urlCaptor.capture(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				any(ParameterizedTypeReference.class)
		);
		assertEquals(CORE_SERVICE_URL + "/api/core/dentist/create-from-user", urlCaptor.getValue());
	}

	@Test
	void createDentistFromUser_ThrowsException() {
		// Given
		CreateDentistFromUserRequest request = new CreateDentistFromUserRequest();
		request.setUserId(1L);
		request.setLicenseNumber("LIC123");

		when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				any(ParameterizedTypeReference.class)
		)).thenThrow(new RestClientException("Connection failed"));

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			coreServiceClient.createDentistFromUser(request);
		});

		assertEquals("Error creating dentist in core microservice", exception.getMessage());
		assertInstanceOf(RestClientException.class, exception.getCause());
	}

	@Test
	void createPatientFromUser_Success() {
		// Given
		CreatePatientFromUserRequest request = new CreatePatientFromUserRequest();
		request.setUserId(2L);
		request.setDni("12345678");

		PatientResponse expectedResponse = new PatientResponse();
		expectedResponse.setId(200L);
		expectedResponse.setUserId(2L);
		expectedResponse.setDni("12345678");

		when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				any(ParameterizedTypeReference.class)
		)).thenReturn(ResponseEntity.ok(expectedResponse));

		// When
		PatientResponse result = coreServiceClient.createPatientFromUser(request);

		// Then
		assertNotNull(result);
		assertEquals(200L, result.getId());
		assertEquals(2L, result.getUserId());
		assertEquals("12345678", result.getDni());

		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(restTemplate).exchange(
				urlCaptor.capture(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				any(ParameterizedTypeReference.class)
		);
		assertEquals(CORE_SERVICE_URL + "/api/core/patient/create-from-user", urlCaptor.getValue());
	}

	@Test
	void createPatientFromUser_ThrowsException() {
		// Given
		CreatePatientFromUserRequest request = new CreatePatientFromUserRequest();
		request.setUserId(2L);

		when(restTemplate.exchange(
				anyString(),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				any(ParameterizedTypeReference.class)
		)).thenThrow(new RestClientException("Timeout"));

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			coreServiceClient.createPatientFromUser(request);
		});

		assertEquals("Error creating patient in core microservice", exception.getMessage());
		assertInstanceOf(RestClientException.class, exception.getCause());
	}

	@Test
	void getDentistIdByUserId_Success() {
		// Given
		Long userId = 1L;
		Long expectedDentistId = 100L;

		when(restTemplate.getForEntity(anyString(), eq(Long.class)))
				.thenReturn(ResponseEntity.ok(expectedDentistId));

		// When
		Long result = coreServiceClient.getDentistIdByUserId(userId);

		// Then
		assertNotNull(result);
		assertEquals(expectedDentistId, result);

		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(restTemplate).getForEntity(urlCaptor.capture(), eq(Long.class));
		assertEquals(CORE_SERVICE_URL + "/api/core/dentist/user-id/1", urlCaptor.getValue());
	}

	@Test
	void getDentistIdByUserId_ReturnsNull_WhenNotFound() {
		// Given
		Long userId = 999L;

		when(restTemplate.getForEntity(anyString(), eq(Long.class)))
				.thenThrow(new RestClientException("Not found"));

		// When
		Long result = coreServiceClient.getDentistIdByUserId(userId);

		// Then
		assertNull(result);
		verify(restTemplate).getForEntity(anyString(), eq(Long.class));
	}

	@Test
	void getPatientIdByUserId_Success() {
		// Given
		Long userId = 2L;
		Long expectedPatientId = 200L;

		when(restTemplate.getForEntity(anyString(), eq(Long.class)))
				.thenReturn(ResponseEntity.ok(expectedPatientId));

		// When
		Long result = coreServiceClient.getPatientIdByUserId(userId);

		// Then
		assertNotNull(result);
		assertEquals(expectedPatientId, result);

		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(restTemplate).getForEntity(urlCaptor.capture(), eq(Long.class));
		assertEquals(CORE_SERVICE_URL + "/api/core/patient/user-id/2", urlCaptor.getValue());
	}

	@Test
	void getPatientIdByUserId_ReturnsNull_WhenNotFound() {
		// Given
		Long userId = 999L;

		when(restTemplate.getForEntity(anyString(), eq(Long.class)))
				.thenThrow(new RestClientException("Not found"));

		// When
		Long result = coreServiceClient.getPatientIdByUserId(userId);

		// Then
		assertNull(result);
		verify(restTemplate).getForEntity(anyString(), eq(Long.class));
	}
}