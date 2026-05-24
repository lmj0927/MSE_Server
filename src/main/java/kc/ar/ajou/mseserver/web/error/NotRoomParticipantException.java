package kc.ar.ajou.mseserver.web.error;

public class NotRoomParticipantException extends RuntimeException {

	public NotRoomParticipantException(String roomId, String userId) {
		super("User is not in room: " + userId + " / " + roomId);
	}
}
