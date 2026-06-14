package kc.ar.ajou.mseserver.web.error;

/** not room participant exception */
public class NotRoomParticipantException extends RuntimeException {

	public NotRoomParticipantException(String roomId, String userId) {
		super("User is not in room: " + userId + " / " + roomId);
	}
}
