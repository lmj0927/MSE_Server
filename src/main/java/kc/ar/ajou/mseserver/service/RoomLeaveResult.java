package kc.ar.ajou.mseserver.service;

import kc.ar.ajou.mseserver.domain.GameRoom;

/** room leave result */
public record RoomLeaveResult(
	boolean deleted, // room deleted when host left
	GameRoom room    // remaining room when guest left
) {
}
