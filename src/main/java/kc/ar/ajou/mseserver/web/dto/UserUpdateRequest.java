package kc.ar.ajou.mseserver.web.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;

public record UserUpdateRequest(
	@Min(0) Integer currency,
	Map<Integer, Integer> gameProgress,
	List<Integer> ownedItems
) {
}
