package kc.ar.ajou.mseserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kc.ar.ajou.mseserver.domain.GameRoom;
import kc.ar.ajou.mseserver.domain.RoomStatus;

/** game room repository */
public interface GameRoomRepository extends JpaRepository<GameRoom, String> {

	// rooms by status (newest first)
	List<GameRoom> findByStatusOrderByCreatedAtDesc(RoomStatus status);
}
