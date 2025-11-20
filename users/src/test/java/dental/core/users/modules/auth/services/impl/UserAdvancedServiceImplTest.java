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
import dental.core.users.services.CoreServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdvancedServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CoreServiceClient coreServiceClient;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserAdvancedServiceImpl userAdvancedService;

	// ==================== GET PROFILE TESTS ====================

	@Test
	void getProfile_Success() {
		// Given
		String email = "user@test.com";
		UserEntity user = createTestUser(1L, email, "John", "Doe", Role.PATIENT);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		// When
		UserProfileRequest result = userAdvancedService.getProfile(email);

		// Then
		assertNotNull(result);
		assertEquals("John", result.getFirstName());
		assertEquals("Doe", result.getLastName());
		verify(userRepository, times(1)).findByEmail(email);
	}

	@Test
	void getProfile_ThrowsException_WhenUserNotFound() {
		// Given
		String email = "notfound@test.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.getProfile(email);
		});

		assertTrue(exception.getMessage().contains("Usuario no encontrado"));
		assertTrue(exception.getMessage().contains(email));
	}

	// ==================== UPDATE PROFILE TESTS ====================

	@Test
	void updateProfile_Success_UpdatesAllFields() {
		// Given
		String email = "user@test.com";
		UserEntity user = createTestUser(1L, email, "John", "Doe", Role.PATIENT);

		UserProfileRequest request = UserProfileRequest.builder()
				.firstName("Jane")
				.lastName("Smith")
				.phone("123456789")
				.address("New Address")
				.birthDate(LocalDate.of(1990, 1, 1))
				.build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		UserProfileRequest result = userAdvancedService.updateProfile(request, email);

		// Then
		assertNotNull(result);
		assertEquals("Jane", user.getFirstName());
		assertEquals("Smith", user.getLastName());
		assertEquals("Jane Smith", user.getName());
		assertEquals("123456789", user.getPhone());
		assertEquals("New Address", user.getAddress());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void updateProfile_PartialUpdate_OnlyFirstName() {
		// Given
		String email = "user@test.com";
		UserEntity user = createTestUser(1L, email, "John", "Doe", Role.PATIENT);
		user.setPhone("111111111");

		UserProfileRequest request = UserProfileRequest.builder()
				.firstName("UpdatedName")
				.build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		userAdvancedService.updateProfile(request, email);

		// Then
		assertEquals("UpdatedName", user.getFirstName());
		assertEquals("Doe", user.getLastName());
		assertEquals("UpdatedName Doe", user.getName());
		assertEquals("111111111", user.getPhone()); // No cambió
	}

	// ==================== CHANGE USER ROLE TESTS ====================

	@Test
	void changeUserRole_Success_AdminChangesRole() {
		// Given
		String adminEmail = "admin@test.com";
		Long userId = 2L;
		Role newRole = Role.DENTIST;

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity user = createTestUser(userId, "user@test.com", "Regular", "User", Role.PATIENT);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		UserProfileRequest result = userAdvancedService.changeUserRole(userId, newRole, adminEmail);

		// Then
		assertNotNull(result);
		assertEquals(Role.DENTIST, user.getRole());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void changeUserRole_ThrowsException_WhenNotAdmin() {
		// Given
		String userEmail = "user@test.com";
		Long targetUserId = 2L;

		UserEntity regularUser = createTestUser(1L, userEmail, "Regular", "User", Role.PATIENT);

		when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(regularUser));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.changeUserRole(targetUserId, Role.DENTIST, userEmail);
		});

		assertTrue(exception.getMessage().contains("Solo los administradores"));
		verify(userRepository, never()).findById(anyLong());
		verify(userRepository, never()).save(any());
	}

	@Test
	void changeUserRole_ThrowsException_WhenTargetUserNotFound() {
		// Given
		String adminEmail = "admin@test.com";
		Long userId = 999L;

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.changeUserRole(userId, Role.DENTIST, adminEmail);
		});

		assertTrue(exception.getMessage().contains("Usuario no encontrado con ID"));
	}

	// ==================== ACTIVATE USER TESTS ====================

	@Test
	void activateUser_Success() {
		// Given
		String adminEmail = "admin@test.com";
		Long userId = 2L;

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity user = createTestUser(userId, "user@test.com", "Regular", "User", Role.PATIENT);
		user.setIsActive(false);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		userAdvancedService.activateUser(userId, adminEmail);

		// Then
		assertTrue(user.getIsActive());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void activateUser_ThrowsException_WhenNotAdmin() {
		// Given
		String userEmail = "user@test.com";
		Long userId = 2L;

		UserEntity regularUser = createTestUser(1L, userEmail, "Regular", "User", Role.PATIENT);

		when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(regularUser));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.activateUser(userId, userEmail);
		});

		assertTrue(exception.getMessage().contains("Solo los administradores"));
	}

	// ==================== DEACTIVATE USER TESTS ====================

	@Test
	void deactivateUser_Success() {
		// Given
		String adminEmail = "admin@test.com";
		Long userId = 2L;

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity user = createTestUser(userId, "user@test.com", "Regular", "User", Role.PATIENT);
		user.setIsActive(true);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(UserEntity.class))).thenReturn(user);

		// When
		userAdvancedService.deactivateUser(userId, adminEmail);

		// Then
		assertFalse(user.getIsActive());
		verify(userRepository, times(1)).save(user);
	}

	// ==================== GET ALL USERS TESTS ====================

	@Test
	void getAllUsers_Success_ReturnsAllUsers() {
		// Given
		String adminEmail = "admin@test.com";
		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity user1 = createTestUser(2L, "user1@test.com", "User", "One", Role.PATIENT);
		UserEntity user2 = createTestUser(3L, "user2@test.com", "User", "Two", Role.DENTIST);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findAll()).thenReturn(Arrays.asList(admin, user1, user2));

		// When
		List<UserDetailResponse> result = userAdvancedService.getAllUsers(adminEmail);

		// Then
		assertNotNull(result);
		assertEquals(3, result.size());
		verify(userRepository, times(1)).findAll();
	}

	@Test
	void getAllUsers_ThrowsException_WhenNotAdmin() {
		// Given
		String userEmail = "user@test.com";
		UserEntity regularUser = createTestUser(1L, userEmail, "Regular", "User", Role.PATIENT);

		when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(regularUser));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.getAllUsers(userEmail);
		});

		assertTrue(exception.getMessage().contains("Solo los administradores"));
		verify(userRepository, never()).findAll();
	}

	// ==================== GET ALL USERS PAGINATED TESTS ====================

	@Test
	void getAllUsersPaginated_Success() {
		// Given
		String adminEmail = "admin@test.com";
		Pageable pageable = PageRequest.of(0, 10);

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity user1 = createTestUser(2L, "user1@test.com", "User", "One", Role.PATIENT);
		UserEntity user2 = createTestUser(3L, "user2@test.com", "User", "Two", Role.DENTIST);

		Page<UserEntity> usersPage = new PageImpl<>(Arrays.asList(admin, user1, user2), pageable, 3);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findAll(pageable)).thenReturn(usersPage);

		// When
		Page<UserDetailResponse> result = userAdvancedService.getAllUsersPaginated(adminEmail, pageable);

		// Then
		assertNotNull(result);
		assertEquals(3, result.getTotalElements());
		assertEquals(3, result.getContent().size());
		verify(userRepository, times(1)).findAll(pageable);
	}

	@Test
	void getUserById_ThrowsException_WhenNotAdmin() {
		// Given
		String userEmail = "user@test.com";
		Long userId = 2L;

		UserEntity regularUser = createTestUser(1L, userEmail, "Regular", "User", Role.PATIENT);

		when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(regularUser));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.getUserById(userId, userEmail);
		});

		assertTrue(exception.getMessage().contains("Solo los administradores"));
	}

	@Test
	void createDentistByAdmin_ThrowsException_WhenUserAlreadyDentist() {
		// Given
		String adminEmail = "admin@test.com";
		CreateDentistAdminRequest request = CreateDentistAdminRequest.builder()
				.email("dentist@test.com")
				.firstName("Already")
				.lastName("Dentist")
				.licenseNumber("LIC789")
				.specialty("General")
				.build();

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity existingDentist = createTestUser(2L, "dentist@test.com", "Already", "Dentist", Role.DENTIST);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findByEmail("dentist@test.com")).thenReturn(Optional.of(existingDentist));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.createDentistByAdmin(request, adminEmail);
		});

		assertTrue(exception.getMessage().contains("ya es dentista"));
		verify(coreServiceClient, never()).createDentistFromUser(any());
	}

	@Test
	void createDentistByAdmin_ThrowsException_WhenConvertingAdmin() {
		// Given
		String adminEmail = "admin@test.com";
		CreateDentistAdminRequest request = CreateDentistAdminRequest.builder()
				.email("another-admin@test.com")
				.firstName("Another")
				.lastName("Admin")
				.licenseNumber("LIC999")
				.specialty("General")
				.build();

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity anotherAdmin = createTestUser(3L, "another-admin@test.com", "Another", "Admin", Role.ADMIN);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findByEmail("another-admin@test.com")).thenReturn(Optional.of(anotherAdmin));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.createDentistByAdmin(request, adminEmail);
		});

		assertTrue(exception.getMessage().contains("No se puede convertir un administrador"));
	}

	@Test
	void createDentistByAdmin_ThrowsException_WhenNotAdmin() {
		// Given
		String userEmail = "user@test.com";
		CreateDentistAdminRequest request = CreateDentistAdminRequest.builder()
				.email("new@test.com")
				.firstName("New")
				.lastName("User")
				.licenseNumber("LIC111")
				.specialty("General")
				.build();

		UserEntity regularUser = createTestUser(1L, userEmail, "Regular", "User", Role.PATIENT);

		when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(regularUser));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.createDentistByAdmin(request, userEmail);
		});

		assertTrue(exception.getMessage().contains("Solo los administradores"));
		verify(coreServiceClient, never()).createDentistFromUser(any());
	}

	@Test
	void createDentistByAdmin_ThrowsException_WhenCoreServiceFails() {
		// Given
		String adminEmail = "admin@test.com";
		CreateDentistAdminRequest request = CreateDentistAdminRequest.builder()
				.email("dentist@test.com")
				.firstName("New")
				.lastName("Dentist")
				.licenseNumber("LIC222")
				.specialty("Orthodontics")
				.build();

		UserEntity admin = createTestUser(1L, adminEmail, "Admin", "User", Role.ADMIN);
		UserEntity newUser = createTestUser(2L, "dentist@test.com", "New", "Dentist", Role.DENTIST);

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
		when(userRepository.findByEmail("dentist@test.com"))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(newUser));
		when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
		when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);
		when(coreServiceClient.createDentistFromUser(any(CreateDentistFromUserRequest.class)))
				.thenThrow(new RuntimeException("Core service error"));

		// When & Then
		UserException exception = assertThrows(UserException.class, () -> {
			userAdvancedService.createDentistByAdmin(request, adminEmail);
		});

		assertTrue(exception.getMessage().contains("Error al crear dentista en el sistema"));
	}

	// ==================== HELPER METHODS ====================

	private UserEntity createTestUser(Long id, String email, String firstName, String lastName, Role role) {
		UserEntity user = UserEntity.builder()
				.email(email)
				.firstName(firstName)
				.lastName(lastName)
				.name(firstName + " " + lastName)
				.role(role)
				.isActive(true)
				.password("encodedPassword")
				.build();
		return user;
	}
}