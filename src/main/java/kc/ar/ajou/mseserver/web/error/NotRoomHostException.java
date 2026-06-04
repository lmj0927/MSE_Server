package kc.ar.ajou.mseserver.web.error;

public class NotRoomHostException extends RuntimeException {

	public NotRoomHostException(String roomId) {
		super("Only the host can perform this action on room: " + roomId);
	}
}
