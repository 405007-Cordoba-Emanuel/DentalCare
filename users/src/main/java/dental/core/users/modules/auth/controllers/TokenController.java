package dental.core.users.modules.auth.controllers;

import dental.core.users.configs.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/token")
@RequiredArgsConstructor
@Tag(name = "Token", description = "Endpoints para información del token")
public class TokenController {

    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    @Operation(summary = "Obtener información del token actual", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getTokenInfo(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("email", jwtUtil.extractEmail(token));
        tokenInfo.put("dentistId", jwtUtil.extractDentistId(token));
        tokenInfo.put("patientId", jwtUtil.extractPatientId(token));
        
        return ResponseEntity.ok(tokenInfo);
    }
}
