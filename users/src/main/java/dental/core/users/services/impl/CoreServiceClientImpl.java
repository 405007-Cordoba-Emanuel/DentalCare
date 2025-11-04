package dental.core.users.services.impl;

import dental.core.users.dto.CreateDentistFromUserRequest;
import dental.core.users.dto.CreatePatientFromUserRequest;
import dental.core.users.dto.DentistResponse;
import dental.core.users.dto.PatientResponse;
import dental.core.users.services.CoreServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CoreServiceClientImpl implements CoreServiceClient {

    @Value("${core.service.url:http://localhost:8082}")
    private String coreServiceUrl;

    private final RestTemplate restTemplate;

    public CoreServiceClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public DentistResponse createDentistFromUser(CreateDentistFromUserRequest request) {
        try {
            String url = coreServiceUrl + "/api/core/dentist/create-from-user";
            log.info("Calling core MS to create dentist: {}", url);
            log.info("Request payload: userId={}, licenseNumber={}, specialty={}", 
                request.getUserId(), request.getLicenseNumber(), request.getSpecialty());
            
            HttpEntity<CreateDentistFromUserRequest> httpEntity = new HttpEntity<>(request);
            ResponseEntity<DentistResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<DentistResponse>() {}
            );
            
            log.info("Response from core MS: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling core microservice to create dentist for userId: {}", request.getUserId(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            throw new RuntimeException("Error creating dentist in core microservice", e);
        }
    }

    @Override
    public PatientResponse createPatientFromUser(CreatePatientFromUserRequest request) {
        try {
            String url = coreServiceUrl + "/api/core/patient/create-from-user";
            log.info("Calling core MS to create patient: {}", url);
            log.info("Request payload: userId={}, dni={}", 
                request.getUserId(), request.getDni());
            
            HttpEntity<CreatePatientFromUserRequest> httpEntity = new HttpEntity<>(request);
            ResponseEntity<PatientResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<PatientResponse>() {}
            );
            
            log.info("Response from core MS: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling core microservice to create patient for userId: {}", request.getUserId(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            throw new RuntimeException("Error creating patient in core microservice", e);
        }
    }

    @Override
    public Long getDentistIdByUserId(Long userId) {
        try {
            String url = coreServiceUrl + "/api/core/dentist/user-id/" + userId;
            ResponseEntity<Long> response = restTemplate.getForEntity(url, Long.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("No dentist found for userId: {}", userId);
            return null;
        }
    }

    @Override
    public Long getPatientIdByUserId(Long userId) {
        try {
            String url = coreServiceUrl + "/api/core/patient/user-id/" + userId;
            ResponseEntity<Long> response = restTemplate.getForEntity(url, Long.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("No patient found for userId: {}", userId);
            return null;
        }
    }

}