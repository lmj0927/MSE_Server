package kc.ar.ajou.mseserver.service;

/** registration and login */
public interface AuthService {

	void register(String userId, String rawPassword);

	String login(String userId, String rawPassword);
}
