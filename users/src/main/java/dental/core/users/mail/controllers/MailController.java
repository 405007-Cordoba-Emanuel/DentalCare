package dental.core.users.mail.controllers;

import dental.core.users.mail.models.EmailRequest;
import dental.core.users.mail.models.EmailType;
import dental.core.users.mail.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/mail")
@RequiredArgsConstructor
@Slf4j
public class MailController {

    private final EmailService emailService;

    /**
     * Endpoint público para enviar emails desde otros microservicios
     * 
     * @param request Objeto con destinatarios, asunto, tipo de email y variables
     * @return ResponseEntity con estado de la operación
     */
    @PostMapping("/send-templated")
    public ResponseEntity<String> sendTemplatedEmail(@RequestBody Map<String, Object> request) {
        try {
            // Extraer datos del request
            @SuppressWarnings("unchecked")
            java.util.List<String> to = (java.util.List<String>) request.get("to");
            String subject = (String) request.get("subject");
            String emailTypeStr = (String) request.get("emailType");
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.get("variables");
            
            // Validar campos requeridos
            if (to == null || to.isEmpty()) {
                return ResponseEntity.badRequest().body("Recipients list is required");
            }
            if (subject == null || subject.isEmpty()) {
                return ResponseEntity.badRequest().body("Subject is required");
            }
            if (emailTypeStr == null || emailTypeStr.isEmpty()) {
                return ResponseEntity.badRequest().body("Email type is required");
            }
            
            // Convertir string a EmailType
            EmailType emailType;
            try {
                emailType = EmailType.valueOf(emailTypeStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid email type: " + emailTypeStr);
            }
            
            // Crear EmailRequest
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(to)
                    .subject(subject)
                    .emailType(emailType)
                    .variables(variables != null ? variables : Map.of())
                    .build();
            
            // Enviar email de forma asíncrona
            emailService.sendTemplatedEmail(emailRequest);
            
            log.info("Email request received and queued for sending to: {}", to);
            return ResponseEntity.ok("Email queued for sending");
            
        } catch (Exception e) {
            log.error("Error processing email request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing email request: " + e.getMessage());
        }
    }
}

