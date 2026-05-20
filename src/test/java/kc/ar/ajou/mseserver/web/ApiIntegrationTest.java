package kc.ar.ajou.mseserver.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void registerLoginRoomsAndJoin() throws Exception {
		register("alpha", "secretpass1");
		register("beta", "secretpass2");

		String tokenAlpha = login("alpha", "secretpass1");
		String tokenBeta = login("beta", "secretpass2");

		mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + tokenBeta))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray());

		String roomJson = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + tokenAlpha)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"Lobby\",\"maxPlayers\":3}")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.hostUserId").value("alpha"))
			.andExpect(jsonPath("$.maxPlayers").value(3))
			.andExpect(jsonPath("$.participantUserIds", hasSize(1)))
			.andReturn()
			.getResponse()
			.getContentAsString();

		String roomId = objectMapper.readTree(roomJson).get("roomId").asText();

		mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + tokenBeta))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)));

		mockMvc
			.perform(
				post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + tokenBeta)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.participantUserIds", hasSize(2)));

		mockMvc
			.perform(
				patch("/api/users/me")
					.header("Authorization", "Bearer " + tokenBeta)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"currency\":50,\"gameProgress\":{\"1\":999},\"ownedItems\":[1,2]}")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.currency").value(50))
			.andExpect(jsonPath("$.gameProgress.1").value(999));
	}

	@Test
	void duplicateRegisterReturnsConflict() throws Exception {
		register("dupuser", "longpassword1");
		mockMvc
			.perform(
				post("/api/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"userId\":\"dupuser\",\"password\":\"otherpass12\"}")
			)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DUPLICATE_USER"));
	}

	@Test
	void joinWhenFullReturnsConflict() throws Exception {
		register("hostx", "secretpass1");
		register("guestx", "secretpass2");
		register("extra", "secretpass3");

		String hostToken = login("hostx", "secretpass1");
		String guestToken = login("guestx", "secretpass2");
		String extraToken = login("extra", "secretpass3");

		MvcResult create = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + hostToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"1v1\",\"maxPlayers\":2}")
			)
			.andExpect(status().isOk())
			.andReturn();

		JsonNode node = objectMapper.readTree(create.getResponse().getContentAsString());
		String roomId = node.get("roomId").asText();

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk());

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + extraToken))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("ROOM_FULL"));
	}

	private void register(String userId, String password) throws Exception {
		mockMvc
			.perform(
				post("/api/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}")
			)
			.andExpect(status().isCreated());
	}

	private String login(String userId, String password) throws Exception {
		MvcResult result = mockMvc
			.perform(
				post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}")
			)
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
	}
}
