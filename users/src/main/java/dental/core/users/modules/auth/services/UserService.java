package dental.core.users.modules.auth.services;

import dental.core.users.entities.UserEntity;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

	UserEntity findUserByEmail(String email);

	void saveUser(UserEntity user);
}
