package kc.ar.ajou.mseserver.service;

import java.util.List;

import kc.ar.ajou.mseserver.domain.GameRoom;

/** game room create, join, start, leave */
public interface RoomService {

	GameRoom createRoom(String hostUserId, String title, int stage, int maxPlayers);

	List<GameRoom> listOpenRooms();

	GameRoom joinRoom(String roomId, String userId);

	RoomLeaveResult leaveRoom(String roomId, String userId);

	GameRoom startRoom(String roomId, String userId);
}
