package dental.core.users.modules.auth.services.impl;

import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleTokenServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private GoogleTokenServiceImpl googleTokenService;

	private static final String CLIENT_ID = "test-client-id";
	private static final String CLIENT_SECRET = "test-client-secret";
	private static final String USER_EMAIL = "test@example.com";
	private static final String ACCESS_TOKEN = "valid-access-token";
	private static final String REFRESH_TOKEN = "valid-refresh-token";
	private static final String NEW_ACCESS_TOKEN = "new-access-token";

	private UserEntity testUser;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(googleTokenService, "clientId", CLIENT_ID);
		ReflectionTestUtils.setField(googleTokenService, "clientSecret", CLIENT_SECRET);

		testUser = new UserEntity();
		testUser.setId(1L);
		testUser.setEmail(USER_EMAIL);
	}

	// ==================== GET VALID ACCESS TOKEN TESTS ====================

	@Test
	void getValidAccessToken_ReturnsExistingToken_WhenTokenIsValid() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusHours(1));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		String result = googleTokenService.getValidAccessToken(USER_EMAIL);

		// Then
		assertNotNull(result);
		assertEquals(ACCESS_TOKEN, result);
		verify(userService, times(2)).findUserByEmail(USER_EMAIL); // Una vez en getValid, otra en isTokenValid
		verify(userRepository, never()).save(any());
	}

	@Test
	void getValidAccessToken_RefreshesToken_WhenTokenIsNull() {
		// Given
		testUser.setGoogleAccessToken(null);
		testUser.setGoogleRefreshToken(REFRESH_TOKEN);
		testUser.setGoogleTokenExpiry(null);

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When & Then
		assertThrows(RuntimeException.class, () -> {
			googleTokenService.getValidAccessToken(USER_EMAIL);
		});
	}

	// ==================== IS TOKEN VALID TESTS ====================

	@Test
	void isTokenValid_ReturnsTrue_WhenTokenIsValidAndNotExpired() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusHours(1));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertTrue(result);
	}

	@Test
	void isTokenValid_ReturnsFalse_WhenTokenIsNull() {
		// Given
		testUser.setGoogleAccessToken(null);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusHours(1));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertFalse(result);
	}

	@Test
	void isTokenValid_ReturnsFalse_WhenExpiryIsNull() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(null);

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertFalse(result);
	}

	@Test
	void isTokenValid_ReturnsFalse_WhenTokenIsExpired() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().minusMinutes(1));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertFalse(result);
	}

	@Test
	void isTokenValid_ReturnsFalse_WhenTokenExpiresInLessThan5Minutes() {
		// Given - Token expira en 4 minutos (menos del margen de 5 minutos)
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusMinutes(4));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertFalse(result);
	}

	@Test
	void isTokenValid_ReturnsTrue_WhenTokenExpiresInMoreThan5Minutes() {
		// Given - Token expira en 6 minutos (más del margen de 5 minutos)
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusMinutes(6));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertTrue(result);
	}

	@Test
	void isTokenValid_ReturnsFalse_WhenTokenExpiresExactlyIn5Minutes() {
		// Given - Token expira exactamente en 5 minutos (en el límite)
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusMinutes(5));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		// Como usa isBefore(expiryTime.minus(5 minutes)), debería retornar false
		assertFalse(result);
	}

	@Test
	void storeTokens_WithZeroExpiresIn_StoresTokensCorrectly() {
		// Given
		long expiresIn = 0L;

		// When
		googleTokenService.storeTokens(testUser, ACCESS_TOKEN, REFRESH_TOKEN, expiresIn);

		// Then
		assertEquals(ACCESS_TOKEN, testUser.getGoogleAccessToken());
		assertEquals(REFRESH_TOKEN, testUser.getGoogleRefreshToken());
		assertNotNull(testUser.getGoogleTokenExpiry());
		verify(userService, times(1)).saveUser(testUser);
	}

	@Test
	void storeTokens_WithLargeExpiresIn_StoresTokensCorrectly() {
		// Given
		long expiresIn = 86400L; // 24 horas

		// When
		googleTokenService.storeTokens(testUser, ACCESS_TOKEN, REFRESH_TOKEN, expiresIn);

		// Then
		assertEquals(ACCESS_TOKEN, testUser.getGoogleAccessToken());
		assertEquals(REFRESH_TOKEN, testUser.getGoogleRefreshToken());

		// Verificar que la expiración está aproximadamente en 24 horas
		LocalDateTime expectedExpiry = LocalDateTime.now().plusSeconds(expiresIn);
		assertTrue(testUser.getGoogleTokenExpiry().isAfter(expectedExpiry.minusSeconds(5)));
		assertTrue(testUser.getGoogleTokenExpiry().isBefore(expectedExpiry.plusSeconds(5)));
	}

	// ==================== REFRESH ACCESS TOKEN TESTS ====================

	@Test
	void refreshAccessToken_ThrowsException_WhenRefreshTokenIsNull() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleRefreshToken(null);

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When & Then
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			googleTokenService.refreshAccessToken(USER_EMAIL);
		});

		assertEquals("El usuario no tiene un refresh token válido", exception.getMessage());
		verify(userRepository, never()).save(any());
	}

	@Test
	void refreshAccessToken_ThrowsException_WhenUserNotFound() {
		// Given
		when(userService.findUserByEmail(USER_EMAIL))
				.thenThrow(new RuntimeException("Usuario no encontrado"));

		// When & Then
		assertThrows(RuntimeException.class, () -> {
			googleTokenService.refreshAccessToken(USER_EMAIL);
		});

		verify(userRepository, never()).save(any());
	}

	@Test
	void refreshAccessToken_ThrowsException_WhenGoogleCredentialFails() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleRefreshToken(REFRESH_TOKEN);

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When & Then
		// GoogleCredential lanzará excepción al intentar crear o refrescar
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			googleTokenService.refreshAccessToken(USER_EMAIL);
		});

		assertTrue(exception.getMessage().contains("Error al renovar el token de acceso"));
	}

	// ==================== EDGE CASES ====================

	@Test
	void isTokenValid_HandlesExactBoundaryConditions() {
		// Given - Token expira exactamente ahora
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now());

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		boolean result = googleTokenService.isTokenValid(USER_EMAIL);

		// Then
		assertFalse(result);
	}

	@Test
	void storeTokens_UpdatesExistingTokens() {
		// Given
		testUser.setGoogleAccessToken("old-token");
		testUser.setGoogleRefreshToken("old-refresh");
		testUser.setGoogleTokenExpiry(LocalDateTime.now().minusHours(1));

		long expiresIn = 3600L;

		// When
		googleTokenService.storeTokens(testUser, NEW_ACCESS_TOKEN, REFRESH_TOKEN, expiresIn);

		// Then
		assertEquals(NEW_ACCESS_TOKEN, testUser.getGoogleAccessToken());
		assertEquals(REFRESH_TOKEN, testUser.getGoogleRefreshToken());
		assertTrue(testUser.getGoogleTokenExpiry().isAfter(LocalDateTime.now()));
	}

	@Test
	void getValidAccessToken_CallsIsTokenValidFirst() {
		// Given
		testUser.setGoogleAccessToken(ACCESS_TOKEN);
		testUser.setGoogleTokenExpiry(LocalDateTime.now().plusHours(2));

		when(userService.findUserByEmail(USER_EMAIL)).thenReturn(testUser);

		// When
		String result = googleTokenService.getValidAccessToken(USER_EMAIL);

		// Then
		assertEquals(ACCESS_TOKEN, result);
		// Verifica que se llamó findUserByEmail (usado en isTokenValid y getValidAccessToken)
		verify(userService, atLeast(2)).findUserByEmail(USER_EMAIL);
	}
}
