package kc.ar.ajou.mseserver.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kc.ar.ajou.mseserver.domain.GameRoom;
import kc.ar.ajou.mseserver.domain.RoomStatus;
import kc.ar.ajou.mseserver.repository.GameRoomRepository;
import kc.ar.ajou.mseserver.web.error.RoomFullException;
import kc.ar.ajou.mseserver.web.error.RoomNotFoundException;
import kc.ar.ajou.mseserver.web.error.RoomNotOpenException;

@Service
public class RoomService {

	private final GameRoomRepository gameRoomRepository;

	public RoomService(GameRoomRepository gameRoomRepository) {
		this.gameRoomRepository = gameRoomRepository;
	}

	@Transactional
	public GameRoom createRoom(String hostUserId, String title, int maxPlayers) {
		GameRoom room = new GameRoom(hostUserId, title, maxPlayers);
		return gameRoomRepository.save(room);
	}

	@Transactional(readOnly = true)
	public List<GameRoom> listOpenRooms() {
		return gameRoomRepository.findByStatusOrderByCreatedAtDesc(RoomStatus.OPEN);
	}

	@Transactional
	public GameRoom joinRoom(String roomId, String userId) {
		GameRoom room = gameRoomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
		boolean ok = room.tryJoin(userId);
		if (!ok) {
			if (room.getStatus() != RoomStatus.OPEN) {
				throw new RoomNotOpenException(roomId);
			}
			throw new RoomFullException(roomId);
		}
		return gameRoomRepository.save(room);
	}
}
