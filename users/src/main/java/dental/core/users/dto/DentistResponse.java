package dental.core.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DentistResponse {
    
    private Long id;
    private Long userId;
    private String licenseNumber;
    private String specialty;
    private Boolean active;
}
