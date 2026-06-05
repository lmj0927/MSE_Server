package kc.ar.ajou.mseserver.web.dto;

import java.util.Map;

import kc.ar.ajou.mseserver.domain.User;

public record UserResponse(
	String userId,
	Map<Integer, Integer> gameProgress
) {

	public static UserResponse from(User user) {
		return new UserResponse(
			user.getUserId(),
			Map.copyOf(user.getGameProgress())
		);
	}
}
