package kc.ar.ajou.mseserver.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kc.ar.ajou.mseserver.domain.User;
import kc.ar.ajou.mseserver.repository.UserRepository;
import kc.ar.ajou.mseserver.web.dto.UserResponse;
import kc.ar.ajou.mseserver.web.error.UserNotFoundException;

@Service
public class UserProfileService {

	private final UserRepository userRepository;

	public UserProfileService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public UserResponse getProfile(String userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		return UserResponse.from(user);
	}

	@Transactional
	public UserResponse updateProfile(
		String userId,
		Integer currency,
		java.util.Map<Integer, Integer> gameProgress,
		java.util.List<Integer> ownedItems
	) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		if (currency != null) {
			user.setCurrency(currency);
		}
		if (gameProgress != null) {
			user.setGameProgress(gameProgress);
		}
		if (ownedItems != null) {
			user.setOwnedItems(ownedItems);
		}
		return UserResponse.from(userRepository.save(user));
	}
}
