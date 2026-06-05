package kc.ar.ajou.mseserver.domain;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@Column(name = "user_id", length = 64)
	private String userId;

	@Column(name = "password_hash", nullable = false, length = 120)
	private String passwordHash;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "user_game_progress", joinColumns = @JoinColumn(name = "user_id"))
	@MapKeyColumn(name = "stage")
	@Column(name = "max_score")
	private Map<Integer, Integer> gameProgress = new HashMap<>();

	protected User() {
	}

	public User(String userId, String passwordHash) {
		this.userId = userId;
		this.passwordHash = passwordHash;
	}

	public String getUserId() {
		return userId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Map<Integer, Integer> getGameProgress() {
		return gameProgress;
	}

	public void setGameProgress(Map<Integer, Integer> gameProgress) {
		this.gameProgress = gameProgress != null ? new HashMap<>(gameProgress) : new HashMap<>();
	}
}
