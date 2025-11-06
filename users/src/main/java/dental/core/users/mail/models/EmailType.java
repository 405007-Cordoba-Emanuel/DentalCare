package dental.core.users.mail.models;

import lombok.Getter;

@Getter
public enum EmailType {
    DEFAULT("default"),
    NOTIFICATION("notification");

    private final String templateName;

    EmailType(String templateName) {
        this.templateName = templateName;
    }
}
