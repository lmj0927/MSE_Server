package kc.ar.ajou.mseserver.web.dto;

import java.util.Map;

public record UserUpdateRequest(
	Map<Integer, Integer> gameProgress
) {
}
