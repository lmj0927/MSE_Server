package kc.ar.ajou.mseserver.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** global API exception handler */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<ErrorBody> duplicate(DuplicateUserException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ErrorBody("DUPLICATE_USER", ex.getMessage()));
	}

	@ExceptionHandler({ RoomNotFoundException.class, UserNotFoundException.class })
	public ResponseEntity<ErrorBody> notFound(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorBody("NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler({ RoomFullException.class })
	public ResponseEntity<ErrorBody> roomFull(RoomFullException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(new ErrorBody("ROOM_FULL", ex.getMessage()));
	}

	@ExceptionHandler({ RoomNotOpenException.class })
	public ResponseEntity<ErrorBody> roomNotOpen(RoomNotOpenException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorBody("ROOM_NOT_OPEN", ex.getMessage()));
	}

	@ExceptionHandler(NotRoomParticipantException.class)
	public ResponseEntity<ErrorBody> notParticipant(NotRoomParticipantException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorBody("NOT_ROOM_PARTICIPANT", ex.getMessage()));
	}

	@ExceptionHandler(NotRoomHostException.class)
	public ResponseEntity<ErrorBody> notHost(NotRoomHostException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(new ErrorBody("NOT_ROOM_HOST", ex.getMessage()));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorBody> badCredentials(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new ErrorBody("BAD_CREDENTIALS", "Invalid userId or password"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException ex) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
			.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
			.findFirst()
			.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorBody("VALIDATION_ERROR", msg));
	}
}
