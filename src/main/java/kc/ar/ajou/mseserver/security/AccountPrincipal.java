package kc.ar.ajou.mseserver.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kc.ar.ajou.mseserver.domain.User;

public class AccountPrincipal implements UserDetails {

	private final String userId;
	private final String passwordHash;

	public AccountPrincipal(String userId, String passwordHash) {
		this.userId = userId;
		this.passwordHash = passwordHash;
	}

	public static AccountPrincipal from(User user) {
		return new AccountPrincipal(user.getUserId(), user.getPasswordHash());
	}

	public String getUserId() {
		return userId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return userId;
	}
}
