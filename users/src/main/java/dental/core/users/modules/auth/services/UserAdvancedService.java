package dental.core.users.modules.auth.services;

import dental.core.users.entities.Role;
import dental.core.users.modules.auth.dto.UserDetailResponse;
import dental.core.users.modules.auth.dto.UserProfileRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio avanzado para la gestión de usuarios.
 * Proporciona métodos para gestión avanzada de perfiles y estadísticas de usuarios.
 */
@Service
public interface UserAdvancedService {
    
    /**
     * Obtiene el perfil del usuario autenticado
     * @param userEmail Email del usuario
     * @return Perfil del usuario
     */
    UserProfileRequest getProfile(String userEmail);

    /**
     * Actualiza el perfil del usuario autenticado
     * @param request Datos del perfil a actualizar
     * @param userEmail Email del usuario
     * @return Usuario actualizado
     */
    UserProfileRequest updateProfile(UserProfileRequest request, String userEmail);

    /**
     * Cambia el rol de un usuario (solo ADMIN)
     * @param userId ID del usuario
     * @param newRole Nuevo rol
     * @param adminEmail Email del administrador
     * @return Usuario actualizado
     */
    UserProfileRequest changeUserRole(Long userId, Role newRole, String adminEmail);
    
    /**
     * Activa un usuario (solo ADMIN)
     * @param userId ID del usuario
     * @param adminEmail Email del administrador
     */
    void activateUser(Long userId, String adminEmail);
    
    /**
     * Desactiva un usuario (solo ADMIN)
     * @param userId ID del usuario
     * @param adminEmail Email del administrador
     */
    void deactivateUser(Long userId, String adminEmail);

    /**
     * Obtiene todos los usuarios (solo ADMIN)
     * @param adminEmail Email del administrador
     * @return Lista de todos los usuarios
     */
    List<UserDetailResponse> getAllUsers(String adminEmail);

    /**
     * Obtiene un usuario por ID (solo ADMIN)
     * @param userId ID del usuario
     * @param adminEmail Email del administrador
     * @return Usuario encontrado
     */
    UserDetailResponse getUserById(Long userId, String adminEmail);
    
}
