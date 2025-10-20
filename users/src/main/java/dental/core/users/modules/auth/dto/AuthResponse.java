package dental.core.users.modules.auth.dto;

import dental.core.users.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	private String id;
    private String token;
    private String firstName;
    private String lastName;
    private String email;
    private String picture;
    private Role role;
}
