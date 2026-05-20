package kc.ar.ajou.mseserver.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
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

	@Column(nullable = false)
	private int currency;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "user_owned_items", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "item_id")
	@OrderColumn(name = "sort_order")
	private List<Integer> ownedItems = new ArrayList<>();

	protected User() {
	}

	public User(String userId, String passwordHash, int currency) {
		this.userId = userId;
		this.passwordHash = passwordHash;
		this.currency = currency;
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

	public int getCurrency() {
		return currency;
	}

	public void setCurrency(int currency) {
		this.currency = currency;
	}

	public List<Integer> getOwnedItems() {
		return ownedItems;
	}

	public void setOwnedItems(List<Integer> ownedItems) {
		this.ownedItems = ownedItems != null ? new ArrayList<>(ownedItems) : new ArrayList<>();
	}
}
