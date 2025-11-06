package dental.core.users.mail.services;

import dental.core.users.mail.models.EmailRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailService {

    void sendTemplatedEmail(EmailRequest dto);

    void sendEmail(List<String> to, String subject, String message);
}
