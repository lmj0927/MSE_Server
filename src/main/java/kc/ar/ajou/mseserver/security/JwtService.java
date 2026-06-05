package kc.ar.ajou.mseserver.security;

public interface JwtService {

	String generateToken(String userId);

	String parseSubject(String token);
}
