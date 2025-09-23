package dental.core.users.modules.auth.exceptions;

/**
 * Excepción personalizada para errores de autenticación.
 */
public class CustomAuthenticationException extends RuntimeException {

    public CustomAuthenticationException(String message) {
        super(message);
    }

    public CustomAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
