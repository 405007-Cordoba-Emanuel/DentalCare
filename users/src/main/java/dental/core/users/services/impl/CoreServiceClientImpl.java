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

    public CoreServiceClientImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public DentistResponse createDentistFromUser(CreateDentistFromUserRequest request) {
        try {
            String url = coreServiceUrl + "/api/core/dentist/create-from-user";
            log.info("Calling core MS to create dentist: {}", url);
            
            HttpEntity<CreateDentistFromUserRequest> httpEntity = new HttpEntity<>(request);
            ResponseEntity<DentistResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<DentistResponse>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling core microservice to create dentist for userId: {}", request.getUserId(), e);
            throw new RuntimeException("Error creating dentist in core microservice", e);
        }
    }

    @Override
    public PatientResponse createPatientFromUser(CreatePatientFromUserRequest request) {
        try {
            String url = coreServiceUrl + "/api/core/patient/create-from-user";
            log.info("Calling core MS to create patient: {}", url);
            
            HttpEntity<CreatePatientFromUserRequest> httpEntity = new HttpEntity<>(request);
            ResponseEntity<PatientResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<PatientResponse>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling core microservice to create patient for userId: {}", request.getUserId(), e);
            throw new RuntimeException("Error creating patient in core microservice", e);
        }
    }
}
