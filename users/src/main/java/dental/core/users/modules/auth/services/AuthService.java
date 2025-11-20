package dental.core.users.modules.auth.services;

import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.ForgotPasswordRequest;
import dental.core.users.modules.auth.dto.LoginRequest;
import dental.core.users.modules.auth.dto.MessageResponse;
import dental.core.users.modules.auth.dto.RegisterRequest;
import dental.core.users.modules.auth.dto.ResetPasswordRequest;

/**
 * Servicio para manejar la autenticación de usuarios.
 * Proporciona métodos para login, registro y recuperación de contraseña.
 */
public interface AuthService {

    /**
     * Autentica un usuario con email y password.
     * 
     * @param loginRequest Datos de login (email y password)
     * @return AuthResponse con token JWT y datos del usuario
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Registra un nuevo usuario.
     * 
     * @param registerRequest Datos de registro (nombre, apellido, email, password, confirmPassword)
     * @return AuthResponse con token JWT y datos del usuario
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * Solicita recuperación de contraseña enviando un email con un token.
     * 
     * @param request Datos de la solicitud (email)
     * @return Mensaje de confirmación
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Resetea la contraseña usando el token recibido por email.
     * 
     * @param request Datos para resetear (token, nueva contraseña)
     * @return Mensaje de confirmación
     */
    MessageResponse resetPassword(ResetPasswordRequest request);
}
