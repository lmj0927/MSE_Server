package kc.ar.ajou.mseserver.web.error;

/** room not open exception */
public class RoomNotOpenException extends RuntimeException {

	public RoomNotOpenException(String roomId) {
		super("Room is not open: " + roomId);
	}
}
