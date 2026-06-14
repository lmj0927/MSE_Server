package kc.ar.ajou.mseserver.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kc.ar.ajou.mseserver.repository.UserRepository;

/** user lookup for Spring Security */
@Service
public class AccountUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public AccountUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findById(username)
			.map(AccountPrincipal::from)
			.orElseThrow(() -> new UsernameNotFoundException(username));
	}
}
