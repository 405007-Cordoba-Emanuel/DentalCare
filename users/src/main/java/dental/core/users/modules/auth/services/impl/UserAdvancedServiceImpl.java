package dental.core.users.modules.auth.services.impl;

import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.dto.UserDetailResponse;
import dental.core.users.modules.auth.dto.UserProfileRequest;
import dental.core.users.modules.auth.exceptions.UserException;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.UserAdvancedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio avanzado de usuarios.
 * Maneja toda la lógica de negocio relacionada con la gestión avanzada de usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAdvancedServiceImpl implements UserAdvancedService {

    private final UserRepository userRepository;

    @Override
    public UserProfileRequest getProfile(String userEmail) {
        log.info("Obteniendo perfil para usuario: {}", userEmail);
        
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserException("Usuario no encontrado: " + userEmail));
        
        return mapToUserProfileRequest(user);
    }

    @Override
    public UserProfileRequest updateProfile(UserProfileRequest request, String userEmail) {
        log.info("Actualizando perfil para usuario: {}", userEmail);
        
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserException("Usuario no encontrado: " + userEmail));
        
        updateUserFields(user, request);
        userRepository.save(user);
        
        return mapToUserProfileRequest(user);
    }

    @Override
    public UserProfileRequest changeUserRole(Long userId, Role newRole, String adminEmail) {
        log.info("Administrador {} cambiando rol de usuario {} a: {}", adminEmail, userId, newRole);
        
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden cambiar roles de usuarios");
        }
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con ID: " + userId));
        
        user.setRole(newRole);
        userRepository.save(user);
        
        return mapToUserProfileRequest(user);
    }

    @Override
    public void activateUser(Long userId, String adminEmail) {
        log.info("Administrador {} activando usuario: {}", adminEmail, userId);
        
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden activar usuarios");
        }
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con ID: " + userId));
        
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long userId, String adminEmail) {
        log.info("Administrador {} desactivando usuario: {}", adminEmail, userId);
        
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden desactivar usuarios");
        }
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con ID: " + userId));
        
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDetailResponse> getAllUsers(String adminEmail) {
        log.info("Administrador {} obteniendo todos los usuarios", adminEmail);
        
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden ver todos los usuarios");
        }
        
        List<UserEntity> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .map(this::mapToUserDetailResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long userId, String adminEmail) {
        log.info("Administrador {} obteniendo usuario con ID: {}", adminEmail, userId);
        
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden obtener usuarios por ID");
        }
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con ID: " + userId));
        
        return mapToUserDetailResponse(user);
    }

    private void updateUserFields(UserEntity user, UserProfileRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        
        // Actualizar el campo name concatenando firstName y lastName
        if (request.getFirstName() != null || request.getLastName() != null) {
            String firstName = request.getFirstName() != null ? request.getFirstName() : user.getFirstName();
            String lastName = request.getLastName() != null ? request.getLastName() : user.getLastName();
            user.setName(firstName + " " + lastName);
        }
    }

    private UserProfileRequest mapToUserProfileRequest(UserEntity user) {
        return UserProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .birthDate(user.getBirthDate())
                .build();
    }

    private UserDetailResponse mapToUserDetailResponse(UserEntity user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .phone(user.getPhone())
                .address(user.getAddress())
                .birthDate(user.getBirthDate())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
