package dental.core.users.modules.auth.services.impl;

import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.exceptions.UserException;
import dental.core.users.modules.auth.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	void findUserByEmail_Success() {
		// Given
		String email = "test@example.com";
		UserEntity expectedUser = new UserEntity();
		expectedUser.setId(1L);
		expectedUser.setEmail(email);
		expectedUser.setName("John Doe");

		when(userRepository.findByEmail(email))
				.thenReturn(Optional.of(expectedUser));

		// When
		UserEntity result = userService.findUserByEmail(email);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals(email, result.getEmail());
		assertEquals("John Doe", result.getName());

		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void findUserByEmail_ThrowsException_WhenUserNotFound() {
		// Given
		String email = "notfound@example.com";

		when(userRepository.findByEmail(email))
				.thenReturn(Optional.empty());

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userService.findUserByEmail(email);
		});

		assertEquals("Usuario no encontrado con email " + email, exception.getMessage());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void findUserByEmail_ThrowsException_WithCorrectMessage() {
		// Given
		String email = "another@test.com";

		when(userRepository.findByEmail(anyString()))
				.thenReturn(Optional.empty());

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userService.findUserByEmail(email);
		});

		assertTrue(exception.getMessage().contains(email));
		assertTrue(exception.getMessage().contains("Usuario no encontrado"));
	}

	@Test
	void saveUser_Success() {
		// Given
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setEmail("save@example.com");
		user.setName("Jane Smith");

		when(userRepository.save(any(UserEntity.class)))
				.thenReturn(user);

		// When
		userService.saveUser(user);

		// Then
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void saveUser_CallsRepositorySave() {
		// Given
		UserEntity user = new UserEntity();
		user.setEmail("test@example.com");

		// When
		userService.saveUser(user);

		// Then
		verify(userRepository, times(1)).save(user);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	void saveUser_WithNullUser_CallsRepository() {
		// Given
		UserEntity user = null;

		// When
		userService.saveUser(user);

		// Then
		verify(userRepository, times(1)).save(null);
	}

	@Test
	void findUserByEmail_WithNullEmail_CallsRepository() {
		// Given
		String email = null;

		when(userRepository.findByEmail(null))
				.thenReturn(Optional.empty());

		// When & Then
		assertThrows(UserException.class, () -> {
			userService.findUserByEmail(email);
		});

		verify(userRepository, times(1)).findByEmail(null);
	}

	@Test
	void findUserByEmail_WithEmptyEmail_ThrowsException() {
		// Given
		String email = "";

		when(userRepository.findByEmail(email))
				.thenReturn(Optional.empty());

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userService.findUserByEmail(email);
		});

		assertEquals("Usuario no encontrado con email ", exception.getMessage());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void saveUser_MultipleUsers_CallsRepositoryForEach() {
		// Given
		UserEntity user1 = new UserEntity();
		user1.setEmail("user1@example.com");

		UserEntity user2 = new UserEntity();
		user2.setEmail("user2@example.com");

		// When
		userService.saveUser(user1);
		userService.saveUser(user2);

		// Then
		verify(userRepository, times(1)).save(user1);
		verify(userRepository, times(1)).save(user2);
		verify(userRepository, times(2)).save(any(UserEntity.class));
	}
}