package dental.core.users.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GoogleTokenRequest {
    @Schema(description = "Google ID token from client authentication")
    @NotBlank(message = "ID token is required")
    private String idToken;
    
    @Schema(description = "Google access token (optional, for Google Calendar access)")
    private String accessToken;
    
    @Schema(description = "Google refresh token (optional, for Google Calendar access)")
    private String refreshToken;
    
    @Schema(description = "Token expiry time in seconds (optional)")
    private Long expiresIn;
}
