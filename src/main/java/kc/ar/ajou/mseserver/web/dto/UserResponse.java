package kc.ar.ajou.mseserver.web.dto;

import java.util.List;
import java.util.Map;

import kc.ar.ajou.mseserver.domain.User;

public record UserResponse(
	String userId,
	Map<Integer, Integer> gameProgress,
	int currency,
	List<Integer> ownedItems
) {

	public static UserResponse from(User user) {
		return new UserResponse(
			user.getUserId(),
			Map.copyOf(user.getGameProgress()),
			user.getCurrency(),
			List.copyOf(user.getOwnedItems())
		);
	}
}
