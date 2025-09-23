package dental.core.users.modules.auth.services;

import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.LoginRequest;
import dental.core.users.modules.auth.dto.RegisterRequest;

/**
 * Servicio para manejar la autenticación de usuarios.
 * Proporciona métodos para login y registro con email y password.
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
}
