package kc.ar.ajou.mseserver.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_rooms")
public class GameRoom {

	@Id
	@Column(length = 36)
	private String roomId;

	@Column(name = "host_user_id", nullable = false, length = 64)
	private String hostUserId;

	@Column(nullable = false, length = 128)
	private String title;

	@Column(nullable = false)
	private int stage;

	@Column(name = "max_players", nullable = false)
	private int maxPlayers;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private RoomStatus status = RoomStatus.OPEN;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "room_participants", joinColumns = @JoinColumn(name = "room_id"))
	@Column(name = "user_id", length = 64)
	@OrderColumn(name = "join_order")
	private List<String> participantUserIds = new ArrayList<>();

	protected GameRoom() {
	}

	public GameRoom(String hostUserId, String title, int stage, int maxPlayers) {
		this.roomId = UUID.randomUUID().toString();
		this.hostUserId = hostUserId;
		this.title = title;
		this.stage = stage;
		this.maxPlayers = maxPlayers;
		this.participantUserIds.add(hostUserId);
	}

	public String getRoomId() {
		return roomId;
	}

	public String getHostUserId() {
		return hostUserId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<String> getParticipantUserIds() {
		return participantUserIds;
	}

	public int getCurrentPlayerCount() {
		return participantUserIds.size();
	}

	/**
	 * @return true if the user is now in the room (including already present)
	 */
	public boolean tryJoin(String userId) {
		if (participantUserIds.contains(userId)) {
			return true;
		}
		if (status != RoomStatus.OPEN) {
			return false;
		}
		if (participantUserIds.size() >= maxPlayers) {
			return false;
		}
		participantUserIds.add(userId);
		return true;
	}

	public boolean isParticipant(String userId) {
		return participantUserIds.contains(userId);
	}

	public boolean isHost(String userId) {
		return hostUserId.equals(userId);
	}

	/**
	 * Removes a non-host participant. Caller must verify the user is not the host.
	 */
	public void removeParticipant(String userId) {
		participantUserIds.remove(userId);
	}

	/**
	 * Transitions OPEN → IN_PROGRESS. No-op if already IN_PROGRESS.
	 *
	 * @return true if transitioned from OPEN; false if already IN_PROGRESS or not allowed
	 */
	public boolean tryStart() {
		if (status == RoomStatus.IN_PROGRESS) {
			return true;
		}
		if (status != RoomStatus.OPEN) {
			return false;
		}
		status = RoomStatus.IN_PROGRESS;
		return true;
	}
}
