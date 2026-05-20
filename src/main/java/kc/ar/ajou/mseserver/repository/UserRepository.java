package kc.ar.ajou.mseserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kc.ar.ajou.mseserver.domain.User;

public interface UserRepository extends JpaRepository<User, String> {

	boolean existsByUserId(String userId);
}
