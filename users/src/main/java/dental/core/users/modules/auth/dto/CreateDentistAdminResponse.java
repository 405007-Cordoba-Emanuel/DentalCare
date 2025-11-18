package dental.core.users.modules.auth.dto;

import dental.core.users.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la creación de dentista por el administrador.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDentistAdminResponse {
    
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private Long dentistId;
    private String licenseNumber;
    private String specialty;
    private boolean userAlreadyExisted;
    private String message;
}

