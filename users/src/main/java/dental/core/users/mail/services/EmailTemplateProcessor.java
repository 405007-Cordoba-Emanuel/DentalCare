package dental.core.users.mail.services;

import java.util.Map;

public interface EmailTemplateProcessor {

    String processTemplate(String template, Map<String, Object> variables);
}
