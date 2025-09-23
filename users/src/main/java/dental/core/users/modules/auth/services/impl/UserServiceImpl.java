package dental.core.users.modules.auth.services.impl;

import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.exceptions.UserException;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public UserEntity findUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserException("Usuario no encontrado con email " + email));
	}

	@Override
	public void saveUser(UserEntity user) {
		userRepository.save(user);
	}
}
