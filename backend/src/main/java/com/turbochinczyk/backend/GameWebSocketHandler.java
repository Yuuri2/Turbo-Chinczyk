package com.turbochinczyk.backend;

import com.turbochinczyk.backend.database.*;
import com.turbochinczyk.backend.message.LobbyMessage;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    private final LobbyManager lobbyManager;
    private final ObjectMapper objectMapper;

    public GameWebSocketHandler(LobbyManager lobbyManager, ObjectMapper objectMapper) {
        this.lobbyManager = lobbyManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String lobbyId = (String) session.getAttributes().get("LOBBY_ID");
        UserSessionDto user = (UserSessionDto) session.getAttributes().get("USER");

        // Connect user to the requested lobby instance
        Lobby lobby = lobbyManager.getOrCreateLobby(lobbyId);
        lobby.handlePlayerJoin(user.getUserId(), user.getUsername(), session);

        System.out.println("Verified User: " + user.getUsername() + " (ID: " + user.getUserId() + ") entered Lobby: " + lobbyId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String lobbyId = (String) session.getAttributes().get("LOBBY_ID");
        Lobby lobby = lobbyManager.get(lobbyId);

        if(lobby != null) {
            try {
                String payload = message.getPayload();
                LobbyMessage lobbyMessage = objectMapper.readValue(payload, LobbyMessage.class);

                lobby.handlePlayerMessage(session, lobbyMessage);
            } catch (Exception e) {
                System.err.println("Jack Son😂 parsowanie nie zadziałało (ai nie dało emotki)");
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String lobbyId = (String) session.getAttributes().get("LOBBY_ID");
        UserSessionDto user = (UserSessionDto) session.getAttributes().get("USER");

        if (lobbyId != null && user != null) {
            Lobby lobby = lobbyManager.getOrCreateLobby(lobbyId);
            lobby.handlePlayerDisconnect(user.getUserId());
        }
    }
}