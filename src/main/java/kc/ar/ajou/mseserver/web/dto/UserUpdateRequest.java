package kc.ar.ajou.mseserver.web.dto;

import java.util.Map;

/** profile update request */
public record UserUpdateRequest(
	Map<Integer, Integer> gameProgress
) {
}
