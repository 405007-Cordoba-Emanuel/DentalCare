package dental.core.users.modules.auth.dto;

import dental.core.users.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para filtros avanzados de búsqueda de usuarios.
 * Permite filtrar usuarios por múltiples criterios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String address;
    private LocalDate birthDateFrom;
    private LocalDate birthDateTo;
    private Boolean isActive;
    
    // Filtros adicionales
    private String phone;
    private Boolean hasProfileImage;
    private Boolean hasGoogleAccount;
}
