package dental.core.users.modules.auth.services;

import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.GoogleTokenRequest;

public interface GoogleAuthService {
    AuthResponse loginOrRegister(GoogleTokenRequest request);
    AuthResponse handleOAuthCallback(String code);
}
