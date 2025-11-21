package dental.core.users.mail.models;

import lombok.Getter;

@Getter
public enum EmailType {
    DEFAULT("default"),
    NOTIFICATION("notification"),
    PASSWORD_RESET("password-reset"),
    APPOINTMENT_CREATED_PATIENT("appointment-created-patient"),
    APPOINTMENT_CREATED_DENTIST("appointment-created-dentist"),
    APPOINTMENT_UPDATED_PATIENT("appointment-updated-patient"),
    APPOINTMENT_UPDATED_DENTIST("appointment-updated-dentist"),
    APPOINTMENT_STATUS_CHANGED_PATIENT("appointment-status-changed-patient"),
    APPOINTMENT_STATUS_CHANGED_DENTIST("appointment-status-changed-dentist");

    private final String templateName;

    EmailType(String templateName) {
        this.templateName = templateName;
    }
}
