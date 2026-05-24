package kc.ar.ajou.mseserver.service;

import kc.ar.ajou.mseserver.domain.GameRoom;

/**
 * @param deleted true when the host left and the room was removed from the database
 * @param room    present when a guest left and the room still exists
 */
public record RoomLeaveResult(boolean deleted, GameRoom room) {
}
