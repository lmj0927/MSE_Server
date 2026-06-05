package kc.ar.ajou.mseserver.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kc.ar.ajou.mseserver.domain.User;
import kc.ar.ajou.mseserver.repository.UserRepository;
import kc.ar.ajou.mseserver.web.dto.UserResponse;
import kc.ar.ajou.mseserver.web.error.UserNotFoundException;

@Service
public class UserProfileServiceImpl implements UserProfileService {

	private final UserRepository userRepository;

	public UserProfileServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getProfile(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		return UserResponse.from(user);
	}

	@Override
	@Transactional
	public UserResponse updateProfile(String userId, Map<Integer, Integer> gameProgress) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		if (gameProgress != null) {
			user.setGameProgress(gameProgress);
		}
		return UserResponse.from(userRepository.save(user));
	}
}
