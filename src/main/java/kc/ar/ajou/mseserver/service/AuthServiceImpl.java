package kc.ar.ajou.mseserver.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kc.ar.ajou.mseserver.domain.User;
import kc.ar.ajou.mseserver.repository.UserRepository;
import kc.ar.ajou.mseserver.security.JwtService;
import kc.ar.ajou.mseserver.web.error.DuplicateUserException;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthServiceImpl(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authenticationManager,
		JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Override
	@Transactional
	public void register(String userId, String rawPassword) {
		if (userRepository.existsByUserId(userId)) {
			throw new DuplicateUserException(userId);
		}
		String hash = passwordEncoder.encode(rawPassword);
		userRepository.save(new User(userId, hash));
	}

	@Override
	public String login(String userId, String rawPassword) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userId, rawPassword));
		return jwtService.generateToken(userId);
	}
}
