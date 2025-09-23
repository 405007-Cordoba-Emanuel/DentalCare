package dental.core.users.modules.auth.dto;

import dental.core.users.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para mostrar información detallada de usuarios.
 * Utilizado por el ADMIN para ver información completa de cualquier usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String picture;
    private String profileImage;
    private Role role;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private Boolean isActive;
    private LocalDateTime lastLogin;

}
