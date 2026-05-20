package kc.ar.ajou.mseserver.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final SecretKey signingKey;
	private final long expirationMs;

	public JwtService(
		@Value("${mse.jwt.secret}") String secret,
		@Value("${mse.jwt.expiration-ms}") long expirationMs
	) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(String userId) {
		Date now = new Date();
		return Jwts.builder()
			.subject(userId)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expirationMs))
			.signWith(signingKey)
			.compact();
	}

	public String parseSubject(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return claims.getSubject();
	}
}
