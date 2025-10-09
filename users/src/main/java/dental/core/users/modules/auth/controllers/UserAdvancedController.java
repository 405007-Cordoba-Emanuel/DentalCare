package dental.core.users.modules.auth.controllers;

import dental.core.users.entities.Role;
import dental.core.users.modules.auth.dto.UserDetailResponse;
import dental.core.users.modules.auth.dto.UserProfileRequest;
import dental.core.users.modules.auth.services.UserAdvancedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller para la gestión avanzada de usuarios.
 * Proporciona endpoints para gestión de perfiles y estadísticas de usuarios.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API para gestión avanzada de usuarios")
public class UserAdvancedController {

    private final UserAdvancedService userAdvancedService;

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil propio", description = "Obtiene el perfil del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<UserProfileRequest> getProfile(
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Usuario {} obteniendo su perfil", authentication.getName());
        UserProfileRequest profile = userAdvancedService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Actualizar perfil propio", description = "Actualiza el perfil del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<UserProfileRequest> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Usuario {} actualizando su perfil", authentication.getName());
        UserProfileRequest updatedProfile = userAdvancedService.updateProfile(request, authentication.getName());
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Cambiar rol de usuario", description = "Cambia el rol de un usuario (solo ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol cambiado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rol inválido o no autorizado"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserProfileRequest> changeUserRole(
            @Parameter(description = "ID del usuario") @PathVariable Long userId,
            @Parameter(description = "Nuevo rol") @RequestParam Role newRole,
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Administrador {} cambiando rol de usuario {} a: {}", authentication.getName(), userId, newRole);
        UserProfileRequest updatedProfile = userAdvancedService.changeUserRole(userId, newRole, authentication.getName());
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/{userId}/activate")
    @Operation(summary = "Activar usuario", description = "Activa un usuario (solo ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "ID del usuario a activar") @PathVariable Long userId,
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Administrador {} activando usuario: {}", authentication.getName(), userId);
        userAdvancedService.activateUser(userId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "Desactivar usuario", description = "Desactiva un usuario (solo ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario desactivado exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "ID del usuario a desactivar") @PathVariable Long userId,
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Administrador {} desactivando usuario: {}", authentication.getName(), userId);
        userAdvancedService.deactivateUser(userId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene la lista de todos los usuarios (solo ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador")
    })
    public ResponseEntity<List<UserDetailResponse>> getAllUsers(@Parameter(hidden = true) Authentication authentication) {
        log.info("Administrador {} obteniendo todos los usuarios", authentication.getName());
        List<UserDetailResponse> users = userAdvancedService.getAllUsers(authentication.getName());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario específico por su ID (solo ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserDetailResponse> getUserById(
            @Parameter(description = "ID del usuario") @PathVariable Long userId,
            @Parameter(hidden = true) Authentication authentication) {
        log.info("Administrador {} obteniendo usuario con ID: {}", authentication.getName(), userId);
        UserDetailResponse user = userAdvancedService.getUserById(userId, authentication.getName());
        return ResponseEntity.ok(user);
    }
}
