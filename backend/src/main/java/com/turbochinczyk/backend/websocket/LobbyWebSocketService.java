package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.database.UserSessionDto;
import com.turbochinczyk.backend.lobby.LobbyService;
import com.turbochinczyk.backend.message.PlayerDisconnectedMessage;
import com.turbochinczyk.backend.message.PlayerReconnectedMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class LobbyWebSocketService {
    private final LobbyService lobbyService;
    private final LobbyConnectionManager connectionManager;

    public LobbyWebSocketService(
            LobbyService lobbyService,
            LobbyConnectionManager connectionManager) {
        this.lobbyService = lobbyService;
        this.connectionManager = connectionManager;
    }

    public void connect(
            int lobbyId,
            UserSessionDto user,
            WebSocketSession session) throws IOException {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        if (!lobby.hasMember(user.getUserId())) {
            throw new LobbyService.LobbyMemberNotFoundException();
        }

        connectionManager.register(lobbyId, user.getUserId(), session);
        connectionManager.broadcast(
                lobbyId,
                new PlayerReconnectedMessage(
                        user.getUserId(),
                        user.getUsername()));
        connectionManager.sendSnapshot(lobby, session);
    }

    public void disconnect(
            int lobbyId,
            UserSessionDto user,
            String sessionId) {
        try {
            Lobby lobby = lobbyService.getLobby(lobbyId);
            if (connectionManager.disconnect(
                    lobbyId,
                    user.getUserId(),
                    sessionId)) {
                lobby.getMember(user.getUserId()).ifPresent(member ->
                        connectionManager.broadcast(
                                lobbyId,
                                new PlayerDisconnectedMessage(
                                        user.getUserId(),
                                        member.getUsername())));
            }
        } catch (LobbyService.LobbyNotFoundException exception) {
            // The lobby was already deleted.
        }
    }
}
