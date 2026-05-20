package kc.ar.ajou.mseserver.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kc.ar.ajou.mseserver.security.AccountPrincipal;
import kc.ar.ajou.mseserver.service.UserProfileService;
import kc.ar.ajou.mseserver.web.dto.UserResponse;
import kc.ar.ajou.mseserver.web.dto.UserUpdateRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserProfileService userProfileService;

	public UserController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal AccountPrincipal principal) {
		return userProfileService.getProfile(principal.getUserId());
	}

	@PatchMapping("/me")
	public UserResponse updateMe(
		@AuthenticationPrincipal AccountPrincipal principal,
		@Valid @RequestBody UserUpdateRequest request
	) {
		return userProfileService.updateProfile(
			principal.getUserId(),
			request.currency(),
			request.gameProgress(),
			request.ownedItems()
		);
	}
}
