package dental.core.users.modules.auth.repositories;

import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad UserEntity.
 * Proporciona métodos para acceder y manipular los datos de usuarios,
 * incluyendo búsquedas por email y validaciones de existencia.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    /**
     * Busca un usuario por su email.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Busca usuarios por rol.
     */
    List<UserEntity> findByRole(Role role);

}
