package dental.core.users.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

	@NotNull(message = "First name is required")
	@Column(name = "first_name", nullable = false)
	private String firstName;

	@NotNull(message = "Last name is required")
	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(name = "name")
	private String name;

	@NotNull(message = "Email is required")
	@Email(message = "Email must be valid")
	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "google_id")
	private String googleId;

	@Column(name = "picture")
	private String picture;

	@Column(name = "google_access_token")
	private String googleAccessToken;
	
	@Column(name = "google_refresh_token")
	private String googleRefreshToken;
	
	@Column(name = "google_token_expiry")
	private LocalDateTime googleTokenExpiry;

	@NotNull(message = "Role is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(name = "phone")
	private String phone;

	@Column(name = "address", length = 500)
	private String address;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "profile_image")
	private String profileImage;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "last_login")
	private LocalDateTime lastLogin;
}
