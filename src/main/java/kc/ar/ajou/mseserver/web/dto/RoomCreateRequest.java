package kc.ar.ajou.mseserver.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomCreateRequest(
	@NotBlank @Size(max = 128) String title,
	@Min(1) int stage,
	@Min(2) @Max(4) int maxPlayers
) {
}
