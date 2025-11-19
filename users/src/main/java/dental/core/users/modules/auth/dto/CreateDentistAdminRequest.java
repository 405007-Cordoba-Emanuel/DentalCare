package dental.core.users.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de dentista por el administrador.
 * Permite crear un dentista directamente con datos de usuario y dentista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDentistAdminRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "License number is required")
    @Size(max = 20, message = "License number must not exceed 20 characters")
    private String licenseNumber;

    @NotBlank(message = "Specialty is required")
    @Size(max = 150, message = "Specialty must not exceed 150 characters")
    private String specialty;
}

