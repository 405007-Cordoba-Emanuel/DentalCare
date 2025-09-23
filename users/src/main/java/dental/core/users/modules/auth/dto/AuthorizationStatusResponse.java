package dental.core.users.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationStatusResponse {
    private boolean isAuthorized;
    private String authorizationUrl;
    private String message;
    private String userEmail;
    private String scopes;
}
