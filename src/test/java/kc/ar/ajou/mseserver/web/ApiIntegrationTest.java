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
					.content("{\"title\":\"Lobby\",\"stage\":2,\"maxPlayers\":3}")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.hostUserId").value("alpha"))
			.andExpect(jsonPath("$.stage").value(2))
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
	void hostLeaveDeletesRoom() throws Exception {
		register("hostleave", "secretpass1");
		register("guestleave", "secretpass2");

		String hostToken = login("hostleave", "secretpass1");
		String guestToken = login("guestleave", "secretpass2");

		MvcResult create = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + hostToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"delete-me\",\"stage\":1,\"maxPlayers\":4}")
			)
			.andExpect(status().isOk())
			.andReturn();

		String roomId = objectMapper.readTree(create.getResponse().getContentAsString()).get("roomId").asText();

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk());

		mockMvc
			.perform(post("/api/rooms/{roomId}/leave", roomId).header("Authorization", "Bearer " + hostToken))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.roomId == '" + roomId + "')]").isEmpty());

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isNotFound());
	}

	@Test
	void guestLeaveRemovesParticipantAndAllowsRejoin() throws Exception {
		register("host2", "secretpass1");
		register("guest2", "secretpass2");

		String hostToken = login("host2", "secretpass1");
		String guestToken = login("guest2", "secretpass2");

		MvcResult create = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + hostToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"stay\",\"stage\":2,\"maxPlayers\":4}")
			)
			.andExpect(status().isOk())
			.andReturn();

		String roomId = objectMapper.readTree(create.getResponse().getContentAsString()).get("roomId").asText();

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.participantUserIds", hasSize(2)));

		mockMvc
			.perform(post("/api/rooms/{roomId}/leave", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.currentPlayerCount").value(1))
			.andExpect(jsonPath("$.participantUserIds", hasSize(1)));

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.participantUserIds", hasSize(2)));
	}

	@Test
	void hostStartSetsInProgressAndExcludesFromOpenList() throws Exception {
		register("starthost", "secretpass1");
		register("startguest", "secretpass2");

		String hostToken = login("starthost", "secretpass1");
		String guestToken = login("startguest", "secretpass2");

		MvcResult create = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + hostToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"match\",\"stage\":1,\"maxPlayers\":4}")
			)
			.andExpect(status().isOk())
			.andReturn();

		String roomId = objectMapper.readTree(create.getResponse().getContentAsString()).get("roomId").asText();

		mockMvc
			.perform(post("/api/rooms/{roomId}/start", roomId).header("Authorization", "Bearer " + hostToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		mockMvc.perform(get("/api/rooms").header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.roomId == '" + roomId + "')]").isEmpty());

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("ROOM_NOT_OPEN"));
	}

	@Test
	void guestCannotStartRoom() throws Exception {
		register("onlyhost", "secretpass1");
		register("onlyguest", "secretpass2");

		String hostToken = login("onlyhost", "secretpass1");
		String guestToken = login("onlyguest", "secretpass2");

		MvcResult create = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + hostToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"no-start\",\"stage\":1,\"maxPlayers\":4}")
			)
			.andExpect(status().isOk())
			.andReturn();

		String roomId = objectMapper.readTree(create.getResponse().getContentAsString()).get("roomId").asText();

		mockMvc
			.perform(post("/api/rooms/{roomId}/join", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isOk());

		mockMvc
			.perform(post("/api/rooms/{roomId}/start", roomId).header("Authorization", "Bearer " + guestToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("NOT_ROOM_HOST"));
	}

	@Test
	void leaveWhenNotParticipantReturnsBadRequest() throws Exception {
		register("outsider", "secretpass1");
		register("roomhost", "secretpass2");

		String hostToken = login("roomhost", "secretpass2");
		String outsiderToken = login("outsider", "secretpass1");

		MvcResult create = mockMvc
			.perform(
				post("/api/rooms")
					.header("Authorization", "Bearer " + hostToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"private\",\"stage\":1,\"maxPlayers\":4}")
			)
			.andExpect(status().isOk())
			.andReturn();

		String roomId = objectMapper.readTree(create.getResponse().getContentAsString()).get("roomId").asText();

		mockMvc
			.perform(post("/api/rooms/{roomId}/leave", roomId).header("Authorization", "Bearer " + outsiderToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("NOT_ROOM_PARTICIPANT"));
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
					.content("{\"title\":\"1v1\",\"stage\":1,\"maxPlayers\":2}")
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
