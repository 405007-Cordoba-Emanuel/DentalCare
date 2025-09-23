package dental.core.users.modules.auth.dto;

import dental.core.users.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para solicitudes de actualización completa de usuario por parte del ADMIN.
 * Permite actualizar todos los campos de un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String picture;
    
    private String profileImage;
    
    private Role role;
    
    private Boolean isActive;
    
    private String googleId;
}
