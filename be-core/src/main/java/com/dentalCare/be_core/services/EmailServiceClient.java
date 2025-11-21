package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.external.EmailRequestDto;

public interface EmailServiceClient {
    
    void sendEmail(EmailRequestDto emailRequest);
}

