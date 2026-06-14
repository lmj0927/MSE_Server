package kc.ar.ajou.mseserver.web.error;

/** room not found exception */
public class RoomNotFoundException extends RuntimeException {

	public RoomNotFoundException(String roomId) {
		super("Room not found: " + roomId);
	}
}
