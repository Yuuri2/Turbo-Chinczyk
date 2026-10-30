package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.LobbySnapshotMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LobbyConnectionManager {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(LobbyConnectionManager.class);

    private final Map<Integer, Map<Integer, WebSocketSession>> connections =
            new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public LobbyConnectionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(int lobbyId, int userId, WebSocketSession session) {
        if (session == null) {
            throw new IllegalArgumentException("WebSocket session must not be null");
        }

        connections
                .computeIfAbsent(lobbyId, ignored -> new ConcurrentHashMap<>())
                .put(userId, session);
    }

    public boolean disconnect(int lobbyId, int userId, String sessionId) {
        Map<Integer, WebSocketSession> lobbyConnections = connections.get(lobbyId);
        if (lobbyConnections == null) {
            return false;
        }

        WebSocketSession currentSession = lobbyConnections.get(userId);
        if (currentSession != null
                && currentSession.getId().equals(sessionId)
                && lobbyConnections.remove(userId, currentSession)) {
            if (lobbyConnections.isEmpty()) {
                connections.remove(lobbyId, lobbyConnections);
            }
            return true;
        }
        return false;
    }

    public boolean isCurrentConnection(
            int lobbyId,
            int userId,
            String sessionId) {
        Map<Integer, WebSocketSession> lobbyConnections = connections.get(lobbyId);
        if (lobbyConnections == null) {
            return false;
        }

        WebSocketSession session = lobbyConnections.get(userId);
        return session != null && session.getId().equals(sessionId);
    }

    public int getUserId(int lobbyId, String sessionId) {
        Map<Integer, WebSocketSession> lobbyConnections = connections.get(lobbyId);
        if (lobbyConnections == null) {
            return -1;
        }

        return lobbyConnections.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getId().equals(sessionId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }

    public Set<Integer> connectedUserIds(int lobbyId) {
        Map<Integer, WebSocketSession> lobbyConnections = connections.get(lobbyId);
        return lobbyConnections == null
                ? Set.of()
                : Set.copyOf(lobbyConnections.keySet());
    }

    public void sendSnapshot(Lobby lobby, WebSocketSession session) throws IOException {
        send(session, new LobbySnapshotMessage(
                lobby.snapshot(connectedUserIds(lobby.getLobbyId()))));
    }

    public void broadcast(int lobbyId, LobbyMessage message) {
        Map<Integer, WebSocketSession> lobbyConnections = connections.get(lobbyId);
        if (lobbyConnections == null) {
            return;
        }

        String json = objectMapper.writeValueAsString(message);
        lobbyConnections.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException exception) {
                LOGGER.warn("Could not send lobby message to WebSocket session {}",
                        session.getId(), exception);
            }
        });
    }

    public void sendTo(
            int lobbyId,
            int userId,
            LobbyMessage message) throws IOException {
        Map<Integer, WebSocketSession> lobbyConnections = connections.get(lobbyId);
        if (lobbyConnections == null) {
            return;
        }

        WebSocketSession session = lobbyConnections.get(userId);
        if (session != null) {
            send(session, message);
        }
    }

    private void send(WebSocketSession session, LobbyMessage message)
            throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(message)));
        }
    }
}
