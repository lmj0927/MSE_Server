package kc.ar.ajou.mseserver.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kc.ar.ajou.mseserver.service.AuthService;
import kc.ar.ajou.mseserver.web.dto.LoginRequest;
import kc.ar.ajou.mseserver.web.dto.LoginResponse;
import kc.ar.ajou.mseserver.web.dto.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public void register(@Valid @RequestBody RegisterRequest request) {
		authService.register(request.userId(), request.password());
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		String token = authService.login(request.userId(), request.password());
		return new LoginResponse(token);
	}
}
