package kc.ar.ajou.mseserver.web.error;

public class DuplicateUserException extends RuntimeException {

	private final String userId;

	public DuplicateUserException(String userId) {
		super("User already exists: " + userId);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
