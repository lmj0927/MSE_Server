package kc.ar.ajou.mseserver.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import kc.ar.ajou.mseserver.domain.User;

@DataJpaTest
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void savesGameProgressAndOwnedItems() {
		User user = new User("player1", "{noop}hash", 100);
		user.setGameProgress(Map.of(1, 500, 2, 1200));
		user.setOwnedItems(List.of(10, 20, 30));
		userRepository.save(user);
		userRepository.flush();

		User loaded = userRepository.findById("player1").orElseThrow();
		assertThat(loaded.getGameProgress()).containsEntry(1, 500).containsEntry(2, 1200);
		assertThat(loaded.getOwnedItems()).containsExactly(10, 20, 30);
		assertThat(loaded.getCurrency()).isEqualTo(100);
	}
}
