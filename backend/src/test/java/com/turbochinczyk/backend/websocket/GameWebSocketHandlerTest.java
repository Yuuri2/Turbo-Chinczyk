package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.LobbyManager;
import com.turbochinczyk.backend.database.UserSessionDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameWebSocketHandlerTest {

    @Test
    void connectionToMissingLobbyIsRejected() throws Exception {
        LobbyManager lobbyManager = new LobbyManager();
        GameWebSocketHandler handler =
                new GameWebSocketHandler(lobbyManager, new ObjectMapper());
        WebSocketSession session = sessionWithAttributes(
                "missing",
                new UserSessionDto(1, "Alice"));

        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test
    void connectionWithoutUserIsRejected() throws Exception {
        LobbyManager lobbyManager = new LobbyManager();
        Lobby lobby = lobbyManager.createLobby("Test lobby", 1, null, 4);
        WebSocketSession session = sessionWithAttributes(lobby.getLobbyId(), null);
        GameWebSocketHandler handler =
                new GameWebSocketHandler(lobbyManager, new ObjectMapper());

        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    private WebSocketSession sessionWithAttributes(
            String lobbyId,
            UserSessionDto user) {
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("LOBBY_ID", lobbyId);
        attributes.put("USER", user);
        when(session.getAttributes()).thenReturn(attributes);
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        return session;
    }
}
