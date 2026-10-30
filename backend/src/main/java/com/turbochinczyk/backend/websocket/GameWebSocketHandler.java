package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.database.UserSessionDto;
import com.turbochinczyk.backend.lobby.LobbyService;
import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.PlayerDisconnectedMessage;
import com.turbochinczyk.backend.message.PlayerReconnectedMessage;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;



@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    private final LobbyService lobbyService;
    private final LobbyConnectionManager connectionManager;
    private final LobbyCommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;

    public GameWebSocketHandler(
            LobbyService lobbyService,
            LobbyConnectionManager connectionManager,
            LobbyCommandDispatcher commandDispatcher,
            ObjectMapper objectMapper) {
        this.lobbyService = lobbyService;
        this.connectionManager = connectionManager;
        this.commandDispatcher = commandDispatcher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer lobbyId = (Integer) session.getAttributes().get("LOBBY_ID");
        UserSessionDto user = (UserSessionDto) session.getAttributes().get("USER");

        if (lobbyId == null || user == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        Lobby lobby;
        try {
            lobby = lobbyService.getLobby(lobbyId);
        } catch (LobbyService.LobbyNotFoundException exception) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        if (!lobby.hasMember(user.getUserId())) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        connectionManager.register(lobbyId, user.getUserId(), session);
        connectionManager.broadcast(
                lobbyId,
                new PlayerReconnectedMessage(user.getUserId(), user.getUsername()));
        connectionManager.sendSnapshot(lobby, session);

        System.out.println("Verified User: " + user.getUsername() + " (ID: " + user.getUserId() + ") entered Lobby: " + lobbyId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Integer lobbyId = (Integer) session.getAttributes().get("LOBBY_ID");
        Lobby lobby;
        try {
            lobby = lobbyService.getLobby(lobbyId);
        } catch (LobbyService.LobbyNotFoundException exception) {
            return;
        }

        try {
                String payload = message.getPayload();
                LobbyMessage lobbyMessage = objectMapper.readValue(payload, LobbyMessage.class);

                commandDispatcher.dispatch(
                        lobbyId,
                        session.getId(),
                        lobbyMessage);
        } catch (Exception exception) {
            System.err.println("Could not process lobby WebSocket message: "
                    + exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer lobbyId = (Integer) session.getAttributes().get("LOBBY_ID");
        UserSessionDto user = (UserSessionDto) session.getAttributes().get("USER");

        if (lobbyId != null && user != null) {
            try {
                Lobby lobby = lobbyService.getLobby(lobbyId);
                if (connectionManager.disconnect(
                        lobbyId, user.getUserId(), session.getId())) {
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
}