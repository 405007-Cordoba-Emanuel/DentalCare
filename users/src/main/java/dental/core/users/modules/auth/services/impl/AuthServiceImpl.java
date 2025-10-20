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
import lombok.RequiredArgsConstructor;
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
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

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

        // Generar token JWT
        String token = jwtUtil.generateToken(savedUser.getId().toString(),savedUser.getEmail(), savedUser.getFirstName(),savedUser.getLastName(), savedUser.getPicture(), savedUser.getRole().name());

        return AuthResponse.builder()
            .token(token)
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .email(savedUser.getEmail())
            .picture(savedUser.getPicture())
            .role(savedUser.getRole())
            .build();
    }
}
