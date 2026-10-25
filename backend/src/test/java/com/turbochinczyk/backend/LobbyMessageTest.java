package com.turbochinczyk.backend;

import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.PlayerLeftMessage;
import com.turbochinczyk.backend.message.LobbySnapshotMessage;
import com.turbochinczyk.backend.message.PlayerReadyMessage;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyMessageTest {

    @Test
    void playerLeftMessageUsesDistinctMessageType() {
        String json = new ObjectMapper().writeValueAsString(
                (LobbyMessage) new PlayerLeftMessage(10, "Alice"));

        assertTrue(json.contains("\"type\":\"PLAYER_LEFT\""));
    }

    @Test
    void lobbySnapshotMessageUsesDistinctMessageType() {
        Lobby lobby = new Lobby("lobby-1", "Test lobby", 1, null, 4);
        String json = new ObjectMapper().writeValueAsString(
                (LobbyMessage) new LobbySnapshotMessage(lobby.snapshot()));

        assertTrue(json.contains("\"type\":\"LOBBY_SNAPSHOT\""));
    }

    @Test
    void playerReadyMessageContainsReadinessState() {
        String json = new ObjectMapper().writeValueAsString(
                (LobbyMessage) new PlayerReadyMessage(10, "Alice", true));

        assertTrue(json.contains("\"type\":\"PLAYER_READY\""));
        assertTrue(json.contains("\"userId\":10"));
        assertTrue(json.contains("\"username\":\"Alice\""));
        assertTrue(json.contains("\"ready\":true"));
    }
}
