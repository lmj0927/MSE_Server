package kc.ar.ajou.mseserver.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kc.ar.ajou.mseserver.security.AccountPrincipal;
import kc.ar.ajou.mseserver.service.RoomLeaveResult;
import kc.ar.ajou.mseserver.service.RoomService;
import kc.ar.ajou.mseserver.web.dto.RoomCreateRequest;
import kc.ar.ajou.mseserver.web.dto.RoomResponse;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@PostMapping
	public RoomResponse create(
		@AuthenticationPrincipal AccountPrincipal principal,
		@Valid @RequestBody RoomCreateRequest request
	) {
		return RoomResponse.from(
			roomService.createRoom(principal.getUserId(), request.title(), request.stage(), request.maxPlayers())
		);
	}

	@GetMapping
	public List<RoomResponse> listOpen() {
		return roomService.listOpenRooms().stream().map(RoomResponse::from).toList();
	}

	@PostMapping("/{roomId}/join")
	public RoomResponse join(
		@AuthenticationPrincipal AccountPrincipal principal,
		@PathVariable String roomId
	) {
		return RoomResponse.from(roomService.joinRoom(roomId, principal.getUserId()));
	}

	@PostMapping("/{roomId}/start")
	public RoomResponse start(
		@AuthenticationPrincipal AccountPrincipal principal,
		@PathVariable String roomId
	) {
		return RoomResponse.from(roomService.startRoom(roomId, principal.getUserId()));
	}

	@PostMapping("/{roomId}/leave")
	public ResponseEntity<RoomResponse> leave(
		@AuthenticationPrincipal AccountPrincipal principal,
		@PathVariable String roomId
	) {
		RoomLeaveResult result = roomService.leaveRoom(roomId, principal.getUserId());
		if (result.deleted()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(RoomResponse.from(result.room()));
	}
}
