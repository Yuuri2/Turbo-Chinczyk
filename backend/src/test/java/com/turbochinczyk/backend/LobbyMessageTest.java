package com.turbochinczyk.backend;

import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.PlayerLeftMessage;
import com.turbochinczyk.backend.message.LobbySnapshotMessage;
import com.turbochinczyk.backend.message.PlayerReadyMessage;
import com.turbochinczyk.backend.message.CommandAcknowledgedMessage;
import com.turbochinczyk.backend.message.CommandRejectedMessage;
import com.turbochinczyk.backend.message.SetReadyCommand;
import com.turbochinczyk.backend.message.StartGameCommand;
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
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
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

    @Test
    void commandMessagesCarryRequestIds() {
        ObjectMapper mapper = new ObjectMapper();

        String commandJson = mapper.writeValueAsString(
                (LobbyMessage) new SetReadyCommand("request-1", true));
        String acknowledgementJson = mapper.writeValueAsString(
                (LobbyMessage) new CommandAcknowledgedMessage(
                        "request-1", "SET_READY"));
        String rejectionJson = mapper.writeValueAsString(
                (LobbyMessage) new CommandRejectedMessage(
                        "request-1",
                        "SET_READY",
                        "LOBBY_RULE_VIOLATION",
                        "Lobby is no longer waiting"));

        assertTrue(commandJson.contains("\"requestId\":\"request-1\""));
        assertTrue(acknowledgementJson.contains("\"type\":\"COMMAND_ACK\""));
        assertTrue(rejectionJson.contains("\"type\":\"COMMAND_REJECTED\""));
        assertTrue(rejectionJson.contains("\"code\":\"LOBBY_RULE_VIOLATION\""));
    }

    @Test
    void startGameCommandUsesTheCommandEnvelope() {
        String json = new ObjectMapper().writeValueAsString(
                (LobbyMessage) new StartGameCommand("request-2"));

        assertTrue(json.contains("\"type\":\"START_GAME\""));
        assertTrue(json.contains("\"requestId\":\"request-2\""));
    }
}
