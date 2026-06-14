package kc.ar.ajou.mseserver.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kc.ar.ajou.mseserver.domain.GameRoom;
import kc.ar.ajou.mseserver.domain.RoomStatus;
import kc.ar.ajou.mseserver.repository.GameRoomRepository;
import kc.ar.ajou.mseserver.web.error.NotRoomHostException;
import kc.ar.ajou.mseserver.web.error.NotRoomParticipantException;
import kc.ar.ajou.mseserver.web.error.RoomFullException;
import kc.ar.ajou.mseserver.web.error.RoomNotFoundException;
import kc.ar.ajou.mseserver.web.error.RoomNotOpenException;

/** game room create, join, start, leave implementation */
@Service
public class RoomServiceImpl implements RoomService {

	private final GameRoomRepository gameRoomRepository;

	public RoomServiceImpl(GameRoomRepository gameRoomRepository) {
		this.gameRoomRepository = gameRoomRepository;
	}

	@Override
	@Transactional
	public GameRoom createRoom(String hostUserId, String title, int stage, int maxPlayers) {
		GameRoom room = new GameRoom(hostUserId, title, stage, maxPlayers);
		return gameRoomRepository.save(room);
	}

	@Override
	@Transactional(readOnly = true)
	public List<GameRoom> listOpenRooms() {
		return gameRoomRepository.findByStatusOrderByCreatedAtDesc(RoomStatus.OPEN);
	}

	@Override
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

	@Override
	@Transactional
	public RoomLeaveResult leaveRoom(String roomId, String userId) {
		GameRoom room = gameRoomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
		if (!room.isParticipant(userId)) {
			throw new NotRoomParticipantException(roomId, userId);
		}
		// delete room when host leaves
		if (room.isHost(userId)) {
			gameRoomRepository.delete(room);
			return new RoomLeaveResult(true, null);
		}
		room.removeParticipant(userId);
		return new RoomLeaveResult(false, gameRoomRepository.save(room));
	}

	@Override
	@Transactional
	public GameRoom startRoom(String roomId, String userId) {
		GameRoom room = gameRoomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
		if (!room.isParticipant(userId)) {
			throw new NotRoomParticipantException(roomId, userId);
		}
		if (!room.isHost(userId)) {
			throw new NotRoomHostException(roomId);
		}
		if (!room.tryStart()) {
			throw new RoomNotOpenException(roomId);
		}
		return gameRoomRepository.save(room);
	}
}
