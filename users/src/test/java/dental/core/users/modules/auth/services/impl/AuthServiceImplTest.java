package dental.core.users.modules.auth.services.impl;

import dental.core.users.configs.security.JwtUtil;
import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import dental.core.users.mail.models.EmailRequest;
import dental.core.users.mail.services.EmailService;
import dental.core.users.modules.auth.dto.*;
import dental.core.users.modules.auth.exceptions.CustomAuthenticationException;
import dental.core.users.modules.auth.exceptions.UserAlreadyExistsException;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.services.CoreServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private CoreServiceClient coreServiceClient;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private AuthServiceImpl authService;

	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PASSWORD = "password123";
	private static final String ENCODED_PASSWORD = "encodedPassword123";
	private static final String FRONTEND_URL = "http://localhost:4200";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(authService, "frontendUrl", FRONTEND_URL);
	}

	@Test
	void login_ThrowsException_WhenCredentialsInvalid() {
		// Given
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setEmail(TEST_EMAIL);
		loginRequest.setPassword("wrongPassword");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Invalid credentials"));

		// When & Then
		CustomAuthenticationException exception = assertThrows(CustomAuthenticationException.class, () -> {
			authService.login(loginRequest);
		});

		assertEquals("Invalid email or password", exception.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void register_ThrowsException_WhenPasswordsDoNotMatch() {
		// Given
		RegisterRequest registerRequest = createRegisterRequest(Role.PATIENT);
		registerRequest.setConfirmPassword("differentPassword");

		// When & Then
		CustomAuthenticationException exception = assertThrows(CustomAuthenticationException.class, () -> {
			authService.register(registerRequest);
		});

		assertEquals("Passwords do not match", exception.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void register_ThrowsException_WhenEmailAlreadyExists() {
		// Given
		RegisterRequest registerRequest = createRegisterRequest(Role.PATIENT);
		UserEntity existingUser = createTestUser(1L, TEST_EMAIL, "Existing", "User", Role.PATIENT);

		when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

		// When & Then
		UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
			authService.register(registerRequest);
		});

		assertEquals("Email already registered", exception.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void register_ThrowsException_WhenRoleIsAdmin() {
		// Given
		RegisterRequest registerRequest = createRegisterRequest(Role.ADMIN);

		// When & Then
		CustomAuthenticationException exception = assertThrows(CustomAuthenticationException.class, () -> {
			authService.register(registerRequest);
		});

		assertTrue(exception.getMessage().contains("Invalid role"));
		verify(userRepository, never()).save(any());
	}

	@Test
	void forgotPassword_Success() {
		// Given
		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail(TEST_EMAIL);

		UserEntity user = createTestUser(1L, TEST_EMAIL, "John", "Doe", Role.PATIENT);

		when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);
		doNothing().when(emailService).sendTemplatedEmail(any(EmailRequest.class));

		// When
		MessageResponse response = authService.forgotPassword(request);

		// Then
		assertNotNull(response);
		assertTrue(response.getMessage().contains("correo electrónico"));

		assertNotNull(user.getResetToken());
		assertNotNull(user.getResetTokenExpiry());
		assertTrue(user.getResetTokenExpiry().isAfter(LocalDateTime.now()));
		assertTrue(user.getResetTokenExpiry().isBefore(LocalDateTime.now().plusHours(2)));

		verify(userRepository, times(1)).save(user);
		verify(emailService, times(1)).sendTemplatedEmail(any(EmailRequest.class));
	}

	@Test
	void forgotPassword_SendsEmailWithCorrectData() {
		// Given
		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail(TEST_EMAIL);

		UserEntity user = createTestUser(1L, TEST_EMAIL, "John", "Doe", Role.PATIENT);

		when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);
		doNothing().when(emailService).sendTemplatedEmail(any(EmailRequest.class));

		// When
		authService.forgotPassword(request);

		// Then
		ArgumentCaptor<EmailRequest> emailCaptor = ArgumentCaptor.forClass(EmailRequest.class);
		verify(emailService).sendTemplatedEmail(emailCaptor.capture());

		EmailRequest sentEmail = emailCaptor.getValue();
		assertTrue(sentEmail.getTo().contains(TEST_EMAIL));
		assertTrue(sentEmail.getSubject().contains("Recuperación de Contraseña"));

		Map<String, Object> variables = sentEmail.getVariables();
		assertTrue(variables.containsKey("userName"));
		assertTrue(variables.containsKey("resetUrl"));
		assertTrue(((String) variables.get("resetUrl")).contains(FRONTEND_URL));
		assertTrue(((String) variables.get("resetUrl")).contains("token="));
	}

	@Test
	void forgotPassword_ThrowsException_WhenUserNotFound() {
		// Given
		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail("nonexistent@example.com");

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.forgotPassword(request);
		});

		assertTrue(exception.getMessage().contains("User not found"));
		verify(emailService, never()).sendTemplatedEmail(any());
	}

	@Test
	void forgotPassword_GeneratesUniqueToken() {
		// Given
		ForgotPasswordRequest request = new ForgotPasswordRequest();
		request.setEmail(TEST_EMAIL);

		UserEntity user = createTestUser(1L, TEST_EMAIL, "John", "Doe", Role.PATIENT);

		when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);
		doNothing().when(emailService).sendTemplatedEmail(any(EmailRequest.class));

		// When
		authService.forgotPassword(request);

		// Then
		assertNotNull(user.getResetToken());
		assertTrue(user.getResetToken().length() > 20); // UUID format
	}

	// ==================== RESET PASSWORD TESTS ====================

	@Test
	void resetPassword_Success() {
		// Given
		String resetToken = "valid-reset-token";
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken(resetToken);
		request.setNewPassword("newPassword123");
		request.setConfirmPassword("newPassword123");

		UserEntity user = createTestUser(1L, TEST_EMAIL, "John", "Doe", Role.PATIENT);
		user.setResetToken(resetToken);
		user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

		when(userRepository.findAll()).thenReturn(java.util.List.of(user));
		when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		MessageResponse response = authService.resetPassword(request);

		// Then
		assertNotNull(response);
		assertTrue(response.getMessage().contains("restablecida exitosamente"));

		assertEquals("encodedNewPassword", user.getPassword());
		assertNull(user.getResetToken());
		assertNull(user.getResetTokenExpiry());

		verify(userRepository, times(1)).save(user);
	}

	@Test
	void resetPassword_ThrowsException_WhenPasswordsDoNotMatch() {
		// Given
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken("valid-token");
		request.setNewPassword("password1");
		request.setConfirmPassword("password2");

		// When & Then
		CustomAuthenticationException exception = assertThrows(CustomAuthenticationException.class, () -> {
			authService.resetPassword(request);
		});

		assertEquals("Passwords do not match", exception.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void resetPassword_ThrowsException_WhenTokenInvalid() {
		// Given
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken("invalid-token");
		request.setNewPassword("newPassword");
		request.setConfirmPassword("newPassword");

		when(userRepository.findAll()).thenReturn(java.util.List.of());

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.resetPassword(request);
		});

		assertTrue(exception.getMessage().contains("Invalid reset token"));
	}

	@Test
	void resetPassword_ThrowsException_WhenTokenExpired() {
		// Given
		String resetToken = "expired-token";
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken(resetToken);
		request.setNewPassword("newPassword");
		request.setConfirmPassword("newPassword");

		UserEntity user = createTestUser(1L, TEST_EMAIL, "John", "Doe", Role.PATIENT);
		user.setResetToken(resetToken);
		user.setResetTokenExpiry(LocalDateTime.now().minusHours(1)); // Expirado

		when(userRepository.findAll()).thenReturn(java.util.List.of(user));

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.resetPassword(request);
		});

		assertTrue(exception.getMessage().contains("expired"));
	}

	@Test
	void resetPassword_ClearsTokenAfterReset() {
		// Given
		String resetToken = "valid-reset-token";
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken(resetToken);
		request.setNewPassword("newPassword123");
		request.setConfirmPassword("newPassword123");

		UserEntity user = createTestUser(1L, TEST_EMAIL, "John", "Doe", Role.PATIENT);
		user.setResetToken(resetToken);
		user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

		when(userRepository.findAll()).thenReturn(java.util.List.of(user));
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		authService.resetPassword(request);

		// Then
		assertNull(user.getResetToken());
		assertNull(user.getResetTokenExpiry());
	}

	// ==================== HELPER METHODS ====================

	private UserEntity createTestUser(Long id, String email, String firstName, String lastName, Role role) {
		return UserEntity.builder()
				.email(email)
				.firstName(firstName)
				.lastName(lastName)
				.name(firstName + " " + lastName)
				.role(role)
				.isActive(true)
				.password(ENCODED_PASSWORD)
				.build();
	}

	private RegisterRequest createRegisterRequest(Role role) {
		RegisterRequest request = new RegisterRequest();
		request.setFirstName("John");
		request.setLastName("Doe");
		request.setEmail(TEST_EMAIL);
		request.setPassword(TEST_PASSWORD);
		request.setConfirmPassword(TEST_PASSWORD);
		request.setRole(role);
		return request;
	}
}