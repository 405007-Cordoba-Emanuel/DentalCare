package dental.core.users.mail.services.implementation;

import dental.core.users.mail.models.EmailRequest;
import dental.core.users.mail.models.MailAttachment;
import dental.core.users.mail.services.EmailService;
import dental.core.users.mail.services.EmailTemplateProcessor;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.w3c.tidy.Tidy;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final Tidy tidy;
    private final EmailTemplateProcessor templateProcessor;

    @Value("${spring.mail.username}")
    private String from;


    /**
     * Sends an email based on the given EmailRequest.
     * <p>
     * This method processes the email template using the given variables and
     * sends the email. If the request contains attachments, it will be sent with
     * the attachments.
     *
     * @param request the email request containing the recipient(s), subject,
     *                email type and variables for the email template and
     *                optional attachments
     */
    @Async
    @Override
    public void sendTemplatedEmail(EmailRequest request) {
        String templateName = request.getEmailType().getTemplateName();
        String processedBody = templateProcessor.processTemplate(templateName, request.getVariables());

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            sendEmailWithAttachments(request.getTo(), request.getSubject(), processedBody, request.getAttachments());
        } else {
            sendEmail(request.getTo(), request.getSubject(), processedBody);
        }
    }

    /**
     * Sends an email using the JavaMailSender.
     *
     * @param to      recipient's email address
     * @param subject subject of the email
     * @param body    body of the email
     */
    @SneakyThrows
    @Override
    public void sendEmail(List<String> to, String subject, String body) {
        if (isInvalidHtml(body)) {
            throw new MessagingException("Invalid HTML body");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom(from);

        mailSender.send(message);
    }

    /**
     * Sends an email with attachments using the JavaMailSender.
     *
     * @param to          recipient's email address
     * @param subject     subject of the email
     * @param body        body of the email
     * @param attachments list of attachments
     */
    @SneakyThrows
    private void sendEmailWithAttachments(
            List<String> to,
            String subject,
            String body,
            List<MailAttachment> attachments
    ) {
        if (isInvalidHtml(body)) {
            throw new MessagingException("Invalid HTML body");
        }

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setTo(to.toArray(new String[0]));
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom(from);

        for (MailAttachment att : attachments) {
            helper.addAttachment(
                    att.getFileName(),
                    new org.springframework.core.io.ByteArrayResource(att.getContent()),
                    att.getContentType()
            );
        }

        mailSender.send(message);
    }

    /**
     * This method validates if the html sent is invalid or not.
     *
     * @param html which represents the HTML string
     * @return if it's invalid or not
     */
    private boolean isInvalidHtml(String html) {
        try (StringReader input = new StringReader(html); StringWriter output = new StringWriter()) {
            tidy.parse(input, output);
            return tidy.getParseErrors() > 0;
        } catch (Exception e) {
            return true;
        }
    }
}
