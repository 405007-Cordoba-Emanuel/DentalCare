package dental.core.users.mail.services.implementation;

import dental.core.users.mail.services.EmailTemplateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateProcessorImpl implements EmailTemplateProcessor {

    private final TemplateEngine templateEngine;

    /**
     * Process the given template name and variables map and returns the processed html.
     *
     * @param templateName the name of the template
     * @param variables    the variables to be used in the template
     * @return the processed html
     */
    @Override
    public String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
