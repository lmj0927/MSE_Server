package kc.ar.ajou.mseserver.security;

/** JWT token generation and parsing */
public interface JwtService {

	String generateToken(String userId);

	String parseSubject(String token);
}
