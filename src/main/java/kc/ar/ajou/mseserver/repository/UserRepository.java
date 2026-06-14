package kc.ar.ajou.mseserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kc.ar.ajou.mseserver.domain.User;

/** user repository */
public interface UserRepository extends JpaRepository<User, String> {

	// check duplicate user id
	boolean existsByUserId(String userId);
}
