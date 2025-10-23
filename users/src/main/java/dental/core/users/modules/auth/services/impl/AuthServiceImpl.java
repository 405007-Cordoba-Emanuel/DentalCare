package dental.core.users.modules.auth.services.impl;

import dental.core.users.configs.security.JwtUtil;
import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.LoginRequest;
import dental.core.users.modules.auth.dto.RegisterRequest;
import dental.core.users.modules.auth.exceptions.CustomAuthenticationException;
import dental.core.users.modules.auth.exceptions.UserAlreadyExistsException;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.AuthService;
import dental.core.users.services.CoreServiceClient;
import dental.core.users.dto.CreateDentistFromUserRequest;
import dental.core.users.dto.CreatePatientFromUserRequest;
import dental.core.users.dto.DentistResponse;
import dental.core.users.dto.PatientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de autenticación.
 * Maneja el login y registro de usuarios con email y password.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CoreServiceClient coreServiceClient;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Obtener usuario autenticado
            UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Actualizar último login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generar token JWT
            String token = jwtUtil.generateToken(user.getId().toString(),user.getEmail(), user.getFirstName(),user.getLastName(), user.getPicture(), user.getRole().name());

            return AuthResponse.builder()
                .token(token)
                .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                .email(user.getEmail())
                .picture(user.getPicture())
                    .role(user.getRole())
                .build();

        } catch (AuthenticationException e) {
            throw new CustomAuthenticationException("Invalid email or password");
        }
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        // Validar que las contraseñas coincidan
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomAuthenticationException("Passwords do not match");
        }

        // Verificar que el email no esté registrado
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Validar que el rol sea PATIENT o DENTIST (no permitir ADMIN desde registro público)
        if (registerRequest.getRole() != Role.PATIENT && registerRequest.getRole() != Role.DENTIST) {
            throw new CustomAuthenticationException("Invalid role. Only PATIENT and DENTIST roles are allowed for registration");
        }

        // Crear nuevo usuario con el rol especificado
        UserEntity newUser = UserEntity.builder()
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .role(registerRequest.getRole()) // Usar el rol enviado por la API
            .isActive(true)
            .lastLogin(LocalDateTime.now())
            .build();

        // Guardar usuario
        UserEntity savedUser = userRepository.save(newUser);

        // Crear dentista o paciente automáticamente en be-core
        Long dentistId = null;
        Long patientId = null;

        if (savedUser.getRole() == Role.DENTIST) {
            try {
                CreateDentistFromUserRequest dentistRequest = new CreateDentistFromUserRequest();
                dentistRequest.setUserId(savedUser.getId());
                dentistRequest.setLicenseNumber("DENT-" + savedUser.getId() + "-AUTO");
                dentistRequest.setSpecialty("Odontología General");

                DentistResponse dentistResponse = coreServiceClient.createDentistFromUser(dentistRequest);
                dentistId = dentistResponse.getId();
            } catch (Exception e) {
                log.error("Error creating dentist in core service: {}", e.getMessage());
                // Continuar sin fallar el registro
            }
        } else if (savedUser.getRole() == Role.PATIENT) {
            try {
                CreatePatientFromUserRequest patientRequest = new CreatePatientFromUserRequest();
                patientRequest.setUserId(savedUser.getId());
                patientRequest.setDni("0000000" + savedUser.getId()); // DNI temporal
                patientRequest.setBirthDate(LocalDateTime.now().minusYears(30).toLocalDate()); // Fecha temporal

                PatientResponse patientResponse = coreServiceClient.createPatientFromUser(patientRequest);
                patientId = patientResponse.getId();
            } catch (Exception e) {
                log.error("Error creating patient in core service: {}", e.getMessage());
                // Continuar sin fallar el registro
            }
        }

        // Generar token JWT con los IDs correspondientes
        String token = jwtUtil.generateToken(savedUser.getId().toString(), savedUser.getEmail(), 
            savedUser.getFirstName(), savedUser.getLastName(), savedUser.getPicture(), 
            savedUser.getRole().name(), dentistId, patientId);

        return AuthResponse.builder()
            .token(token)
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .email(savedUser.getEmail())
            .picture(savedUser.getPicture())
            .role(savedUser.getRole())
            .dentistId(dentistId)
            .patientId(patientId)
            .build();
    }
}
