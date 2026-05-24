package kc.ar.ajou.mseserver.web.dto;

import java.time.Instant;
import java.util.List;

import kc.ar.ajou.mseserver.domain.GameRoom;
import kc.ar.ajou.mseserver.domain.RoomStatus;

public record RoomResponse(
	String roomId,
	String hostUserId,
	String title,
	int stage,
	int maxPlayers,
	int currentPlayerCount,
	RoomStatus status,
	Instant createdAt,
	List<String> participantUserIds
) {

	public static RoomResponse from(GameRoom room) {
		return new RoomResponse(
			room.getRoomId(),
			room.getHostUserId(),
			room.getTitle(),
			room.getStage(),
			room.getMaxPlayers(),
			room.getCurrentPlayerCount(),
			room.getStatus(),
			room.getCreatedAt(),
			List.copyOf(room.getParticipantUserIds())
		);
	}
}
