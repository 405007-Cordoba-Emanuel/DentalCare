package dental.core.users.modules.auth.controllers;

import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.GoogleTokenRequest;
import dental.core.users.modules.auth.services.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/users/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody GoogleTokenRequest request) {
        log.info("Iniciando autenticación con Google");
        
        AuthResponse response = googleAuthService.loginOrRegister(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> getAuthorizationUrl() {
        // Esta URL debe ser construida con los scopes correctos
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=830906165893-c7i1u8o134ej796cgoihpm9secct665m.apps.googleusercontent.com" +
                "&redirect_uri=http://localhost:8080/api/auth/google/callback" +
                "&response_type=code" +
                "&scope=openid%20profile%20email" +
                "&access_type=offline" +
                "&prompt=consent";
        
        return ResponseEntity.ok(authUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam("code") String code) {
        log.info("Recibido código de autorización: {}", code);
        
        try {
            googleAuthService.handleOAuthCallback(code);
            
            // Redirigir al frontend con los datos de autenticación
            String frontendUrl = "http://localhost:4200/auth/profile";
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error en callback de OAuth: {}", e.getMessage(), e);
            
            // Redirigir al frontend con error
            String errorUrl = "http://localhost:4200/auth/callback?error=" + URLEncoder.encode("OAuth callback failed: " + e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", errorUrl)
                    .build();
        }
    }
}
