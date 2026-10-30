package com.turbochinczyk.backend.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class LobbyConnectionManagerTest {

    @Test
    void reconnectReplacesTheCurrentConnection() {
        LobbyConnectionManager manager =
                new LobbyConnectionManager(new ObjectMapper());
        WebSocketSession first = session("session-1");
        WebSocketSession second = session("session-2");

        manager.register(1, 1, first);
        manager.register(1, 1, second);

        assertFalse(manager.isCurrentConnection(1, 1, "session-1"));
        assertTrue(manager.isCurrentConnection(1, 1, "session-2"));
    }

    @Test
    void staleDisconnectDoesNotRemoveReplacementConnection() {
        LobbyConnectionManager manager =
                new LobbyConnectionManager(new ObjectMapper());
        WebSocketSession first = session("session-1");
        WebSocketSession second = session("session-2");

        manager.register(1, 1, first);
        manager.register(1, 1, second);

        assertFalse(manager.disconnect(1, 1, "session-1"));
        assertTrue(manager.isCurrentConnection(1, 1, "session-2"));
    }

    @Test
    void closingLobbyClosesAndRemovesAllConnections() throws Exception {
        LobbyConnectionManager manager =
                new LobbyConnectionManager(new ObjectMapper());
        WebSocketSession first = session("session-1");
        WebSocketSession second = session("session-2");

        manager.register(1, 1, first);
        manager.register(1, 2, second);

        manager.closeLobby(1, new CloseStatus(4004, "Lobby was deleted"));

        verify(first).close(new CloseStatus(4004, "Lobby was deleted"));
        verify(second).close(new CloseStatus(4004, "Lobby was deleted"));
        assertTrue(manager.connectedUserIds(1).isEmpty());
    }

    private WebSocketSession session(String sessionId) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.isOpen()).thenReturn(true);
        return session;
    }
}
