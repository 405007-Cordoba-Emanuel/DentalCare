package dental.core.users.mail.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class MailAttachment {

    private String fileName;

    private String contentType;

    private byte[] content;
}
