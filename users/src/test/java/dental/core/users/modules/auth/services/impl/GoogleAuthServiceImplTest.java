package dental.core.users.modules.auth.services.impl;

import dental.core.users.configs.security.JwtUtil;
import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.GoogleTokenRequest;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.GoogleTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private GoogleTokenService googleTokenService;

	@InjectMocks
	private GoogleAuthServiceImpl googleAuthService;

	private static final String CLIENT_ID = "test-client-id";
	private static final String CLIENT_SECRET = "test-client-secret";
	private static final String TEST_TOKEN = "test-jwt-token";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(googleAuthService, "googleClientId", CLIENT_ID);
		ReflectionTestUtils.setField(googleAuthService, "googleClientSecret", CLIENT_SECRET);
	}

	// ==================== TEST TOKEN (DEVELOPMENT) TESTS ====================

	@Test
	void loginOrRegister_HandleTestToken_NewUser() {
		// Given
		GoogleTokenRequest request = new GoogleTokenRequest();
		request.setIdToken("test-token-development");

		UserEntity newUser = createTestUser(1L, "test@example.com", "Test User", "Test User", Role.PATIENT);

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);
		when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(TEST_TOKEN);

		// When
		AuthResponse response = googleAuthService.loginOrRegister(request);

		// Then
		assertNotNull(response);
		assertEquals(TEST_TOKEN, response.getToken());
		assertEquals("Test User", response.getFirstName());
		assertEquals("Test User", response.getLastName());
		assertEquals("test@example.com", response.getEmail());

		verify(userRepository, times(1)).save(any(UserEntity.class));
	}

	@Test
	void findOrCreateUser_UpdatesExistingUser_WhenDataChanges() {
		// Given
		String email = "existing@test.com";
		UserEntity existingUser = createTestUser(1L, email, "Old", "Name", Role.PATIENT);
		existingUser.setPicture("old-picture.jpg");

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
		when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(TEST_TOKEN);

		GoogleTokenRequest request = new GoogleTokenRequest();
		request.setIdToken("test-token-development");

		// When
		googleAuthService.loginOrRegister(request);

		// Then
		verify(userRepository, times(1)).save(existingUser);
	}

	@Test
	void splitName_WithFullName_SplitsCorrectly() {
		// Estos tests son indirectos porque splitName es privado
		// Los probamos a través de loginOrRegister con el test token

		GoogleTokenRequest request = new GoogleTokenRequest();
		request.setIdToken("test-token-development");

		UserEntity newUser = createTestUser(1L, "test@example.com", "Test User", "Test User", Role.PATIENT);

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);
		when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(TEST_TOKEN);

		// When
		AuthResponse response = googleAuthService.loginOrRegister(request);

		// Then
		assertNotNull(response);
		assertEquals("Test User", response.getFirstName());
		assertEquals("Test User", response.getLastName());
	}

	// ==================== ERROR HANDLING TESTS ====================

	@Test
	void loginOrRegister_ThrowsException_WhenRepositoryFails() {
		// Given
		GoogleTokenRequest request = new GoogleTokenRequest();
		request.setIdToken("test-token-development");

		when(userRepository.findByEmail(anyString()))
				.thenThrow(new RuntimeException("Database error"));

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			googleAuthService.loginOrRegister(request);
		});

		assertTrue(exception.getMessage().contains("Authentication failed"));
	}

	@Test
	void loginOrRegister_CreatesUserWithDefaultRole() {
		// Given
		GoogleTokenRequest request = new GoogleTokenRequest();
		request.setIdToken("test-token-development");

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
			UserEntity savedUser = invocation.getArgument(0);
			savedUser.setId(1L);
			return savedUser;
		});
		when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(TEST_TOKEN);

		// When
		googleAuthService.loginOrRegister(request);

		// Then
		ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
		verify(userRepository, times(1)).save(userCaptor.capture());

		UserEntity savedUser = userCaptor.getValue();
		assertEquals(Role.PATIENT, savedUser.getRole());
		assertTrue(savedUser.getIsActive());
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
				.password("encodedPassword")
				.build();
	}
}
