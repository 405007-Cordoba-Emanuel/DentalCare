package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.dtos.external.EmailRequestDto;
import com.dentalCare.be_core.services.EmailServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmailServiceClientImpl implements EmailServiceClient {

    @Value("${users.service.url:http://localhost:8081}")
    private String usersServiceUrl;

    private final RestTemplate restTemplate;

    public EmailServiceClientImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendEmail(EmailRequestDto emailRequest) {
        try {
            String url = usersServiceUrl + "/public/mail/send-templated";
            log.info("Sending email to users MS: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<EmailRequestDto> request = new HttpEntity<>(emailRequest, headers);
            
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Email sent successfully to: {}", emailRequest.getTo());
        } catch (Exception e) {
            log.error("Error sending email through users microservice: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo principal de creación de citas
            // El email es una funcionalidad adicional, no crítica
        }
    }
}

