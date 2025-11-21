package com.dentalCare.be_core.dtos.external;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequestDto {
    
    private List<String> to;
    private String subject;
    private String emailType;
    private Map<String, Object> variables;
}

