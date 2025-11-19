package dental.core.users.modules.auth.controllers;

import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.ForgotPasswordRequest;
import dental.core.users.modules.auth.dto.LoginRequest;
import dental.core.users.modules.auth.dto.MessageResponse;
import dental.core.users.modules.auth.dto.RegisterRequest;
import dental.core.users.modules.auth.dto.ResetPasswordRequest;
import dental.core.users.modules.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para manejar la autenticación de usuarios.
 * Proporciona endpoints para login, registro y recuperación de contraseña.
 * CORS es manejado por el Gateway - no configurar aquí
 */
@RestController
@RequestMapping("/api/users/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para login de usuarios.
     * 
     * @param loginRequest Datos de login (email y password)
     * @return AuthResponse con token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registro de usuarios.
     * 
     * @param registerRequest Datos de registro (nombre, apellido, email, password, confirmPassword)
     * @return AuthResponse con token JWT y datos del usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para solicitar recuperación de contraseña.
     * Envía un email con un link para resetear la contraseña.
     * 
     * @param request Datos de la solicitud (email)
     * @return Mensaje de confirmación
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para resetear la contraseña usando un token.
     * 
     * @param request Datos para resetear (token, nueva contraseña)
     * @return Mensaje de confirmación
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
