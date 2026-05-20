package kc.ar.ajou.mseserver.web.error;

public class RoomNotOpenException extends RuntimeException {

	public RoomNotOpenException(String roomId) {
		super("Room is not open: " + roomId);
	}
}
