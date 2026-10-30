package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.database.UserSessionDto;
import com.turbochinczyk.backend.lobby.LobbyService;
import com.turbochinczyk.backend.message.LobbyMessage;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;



@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    private final LobbyWebSocketService webSocketService;
    private final LobbyCommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;

    public GameWebSocketHandler(
            LobbyWebSocketService webSocketService,
            LobbyCommandDispatcher commandDispatcher,
            ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
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

        try {
            webSocketService.connect(lobbyId, user, session);
        } catch (LobbyService.LobbyNotFoundException
                 | LobbyService.LobbyMemberNotFoundException exception) {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Integer lobbyId = (Integer) session.getAttributes().get("LOBBY_ID");
        if (lobbyId == null) {
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
            webSocketService.disconnect(lobbyId, user, session.getId());
        }
    }
}