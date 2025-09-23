package dental.core.users.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de estadísticas de usuarios.
 * Contiene métricas y estadísticas del sistema de usuarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long adminUsers;
    private Long ownerUsers;
    private Long clientUsers;
    private Long newUsersThisMonth;
    private Long newUsersThisWeek;
    private LocalDateTime lastUserRegistration;
    private Double averageUserAge;
    private Long usersWithProfileImage;
    private Long usersWithPhone;
    private Long usersWithAddress;
}
