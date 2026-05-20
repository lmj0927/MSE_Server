package kc.ar.ajou.mseserver.web.error;

public class RoomFullException extends RuntimeException {

	public RoomFullException(String roomId) {
		super("Room is full: " + roomId);
	}
}
