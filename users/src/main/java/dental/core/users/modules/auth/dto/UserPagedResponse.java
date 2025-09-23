package dental.core.users.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuesta paginada de usuarios.
 * Contiene la lista de usuarios y metadatos de paginación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPagedResponse {
    
    private List<UserDetailResponse> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
}
