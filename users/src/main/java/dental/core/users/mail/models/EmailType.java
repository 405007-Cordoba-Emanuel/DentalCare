package dental.core.users.mail.models;

import lombok.Getter;

@Getter
public enum EmailType {
    DEFAULT("default"),
    NOTIFICATION("notification"),
    PASSWORD_RESET("password-reset"),
    APPOINTMENT_CREATED_PATIENT("appointment-created-patient"),
    APPOINTMENT_CREATED_DENTIST("appointment-created-dentist");

    private final String templateName;

    EmailType(String templateName) {
        this.templateName = templateName;
    }
}
