package dental.core.users.modules.auth.services;

import dental.core.users.entities.UserEntity;

public interface GoogleTokenService {
    
    /**
     * Obtiene el token de acceso válido para un usuario
     * @param userEmail Email del usuario
     * @return Token de acceso válido
     */
    String getValidAccessToken(String userEmail);
    
    /**
     * Renueva el token de acceso usando el refresh token
     * @param userEmail Email del usuario
     * @return Nuevo token de acceso
     */
    String refreshAccessToken(String userEmail);
    
    /**
     * Almacena los tokens de Google para un usuario
     * @param user Usuario
     * @param accessToken Token de acceso
     * @param refreshToken Token de refresh
     * @param expiresIn Tiempo de expiración en segundos
     */
    void storeTokens(UserEntity user, String accessToken, String refreshToken, long expiresIn);
    
    /**
     * Verifica si el token de acceso del usuario es válido
     * @param userEmail Email del usuario
     * @return true si el token es válido, false en caso contrario
     */
    boolean isTokenValid(String userEmail);
}
