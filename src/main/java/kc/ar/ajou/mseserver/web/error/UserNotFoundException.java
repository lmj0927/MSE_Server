package kc.ar.ajou.mseserver.web.error;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(String userId) {
		super("User not found: " + userId);
	}
}
