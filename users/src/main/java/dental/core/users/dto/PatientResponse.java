package dental.core.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    
    private Long id;
    private Long userId;
    private String dni;
    private LocalDate birthDate;
    private Boolean active;
}
