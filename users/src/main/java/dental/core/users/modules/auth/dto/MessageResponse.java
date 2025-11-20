package dental.core.users.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO genérico para respuestas con mensaje.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
}

