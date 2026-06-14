package kc.ar.ajou.mseserver.web.error;

/** room full exception */
public class RoomFullException extends RuntimeException {

	public RoomFullException(String roomId) {
		super("Room is full: " + roomId);
	}
}
