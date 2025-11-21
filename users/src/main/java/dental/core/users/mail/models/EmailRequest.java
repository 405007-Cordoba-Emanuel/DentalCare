package dental.core.users.mail.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object (DTO) representing the creation of an email.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {

    private List<String> to;

    private String subject;

    private EmailType emailType;

    private Map<String, Object> variables;

    private List<MailAttachment> attachments;
}
