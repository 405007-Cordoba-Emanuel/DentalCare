package dental.core.users.modules.auth.services.impl;

import dental.core.users.dto.CreateDentistFromUserRequest;
import dental.core.users.dto.DentistResponse;
import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.dto.CreateDentistAdminRequest;
import dental.core.users.modules.auth.dto.CreateDentistAdminResponse;
import dental.core.users.modules.auth.dto.UserDetailResponse;
import dental.core.users.modules.auth.dto.UserProfileRequest;
import dental.core.users.modules.auth.exceptions.UserException;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.UserAdvancedService;
import dental.core.users.services.CoreServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    private final CoreServiceClient coreServiceClient;
    private final PasswordEncoder passwordEncoder;

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
    public Page<UserDetailResponse> getAllUsersPaginated(String adminEmail, Pageable pageable) {
        log.info("Administrador {} obteniendo usuarios paginados", adminEmail);
        
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden ver todos los usuarios");
        }
        
        Page<UserEntity> usersPage = userRepository.findAll(pageable);
        
        return usersPage.map(this::mapToUserDetailResponse);
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

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CreateDentistAdminResponse createDentistByAdmin(CreateDentistAdminRequest request, String adminEmail) {
        log.info("Administrador {} creando dentista para email: {}", adminEmail, request.getEmail());
        
        // Verificar que quien ejecuta sea ADMIN
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserException("Administrador no encontrado: " + adminEmail));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new UserException("Solo los administradores pueden crear dentistas");
        }
        
        // Crear o actualizar usuario en una transacción separada
        UserEntity user = createOrUpdateUserForDentist(request);
        boolean userAlreadyExisted = user.getId() != null && userRepository.findByEmail(request.getEmail()).isPresent();
        
        log.info("Usuario guardado con ID: {}. Llamando al microservicio de Core para crear dentista", user.getId());
        
        // Crear el dentista en el microservicio de core
        CreateDentistFromUserRequest dentistRequest = new CreateDentistFromUserRequest();
        dentistRequest.setUserId(user.getId());
        dentistRequest.setLicenseNumber(request.getLicenseNumber());
        dentistRequest.setSpecialty(request.getSpecialty());
        
        DentistResponse dentistResponse;
        try {
            dentistResponse = coreServiceClient.createDentistFromUser(dentistRequest);
            log.info("Dentista creado exitosamente en el microservicio core con ID: {}", dentistResponse.getId());
        } catch (Exception e) {
            log.error("Error al crear dentista en el microservicio core", e);
            throw new UserException("Error al crear dentista en el sistema: " + e.getMessage());
        }
        
        // Construir y retornar la respuesta
        String message = userAlreadyExisted 
            ? "Usuario existente actualizado a dentista y dentista creado exitosamente"
            : "Nuevo usuario y dentista creados exitosamente";
        
        return CreateDentistAdminResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .dentistId(dentistResponse.getId())
                .licenseNumber(request.getLicenseNumber())
                .specialty(request.getSpecialty())
                .userAlreadyExisted(userAlreadyExisted)
                .message(message)
                .build();
    }

    /**
     * Crea o actualiza un usuario para convertirlo en dentista.
     * Este método se ejecuta en una transacción independiente que se commitea
     * inmediatamente, permitiendo que otros servicios vean el usuario.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private UserEntity createOrUpdateUserForDentist(CreateDentistAdminRequest request) {
        // Buscar si existe un usuario con ese email
        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(request.getEmail());
        UserEntity user;
        
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            
            // Si ya es dentista, lanzar error
            if (user.getRole() == Role.DENTIST) {
                throw new UserException("El usuario con email " + request.getEmail() + " ya es dentista");
            }
            
            // Si es paciente, cambiar el rol a dentista
            if (user.getRole() == Role.PATIENT) {
                log.info("Usuario {} es paciente, cambiando rol a DENTIST", request.getEmail());
                user.setRole(Role.DENTIST);
                
                // Actualizar nombre si es necesario
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setName(request.getFirstName() + " " + request.getLastName());
                
                user = userRepository.save(user);
            } else if (user.getRole() == Role.ADMIN) {
                throw new UserException("No se puede convertir un administrador en dentista");
            }
        } else {
            // Crear nuevo usuario con rol DENTIST y password "123456"
            log.info("Creando nuevo usuario con email: {} y rol DENTIST", request.getEmail());
            
            String encodedPassword = passwordEncoder.encode("123456");
            
            user = UserEntity.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .name(request.getFirstName() + " " + request.getLastName())
                    .password(encodedPassword)
                    .role(Role.DENTIST)
                    .isActive(true)
                    .build();
            
            user = userRepository.save(user);
        }
        
        return user;
    }
}
