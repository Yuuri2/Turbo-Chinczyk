package com.turbochinczyk.backend;

import com.turbochinczyk.backend.database.*;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    private final LobbyManager lobbyManager;
    private final AuthService authService;

    public GameWebSocketHandler(LobbyManager lobbyManager, AuthService authService) {
        this.lobbyManager = lobbyManager;
        this.authService = authService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String lobbyId = (String)session.getAttributes().get("LOBBY_ID");
        UserSessionDto user = (UserSessionDto)session.getAttributes().get("PLAYER");

        // Connect user to the requested lobby instance
        Lobby lobby = lobbyManager.getOrCreateLobby(lobbyId);
        lobby.handlePlayerJoin(user.getUserId(), user.getUsername(), session);

        System.out.println("Verified User: " + user.getUsername() + " (ID: " + user.getUserId() + ") entered Lobby: " + lobbyId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String lobbyId = (String) session.getAttributes().get("lobbyId");
        Integer userId = (Integer) session.getAttributes().get("userId");

        if (lobbyId != null && userId != null) {
            Lobby lobby = lobbyManager.getOrCreateLobby(lobbyId);
            lobby.handlePlayerDisconnect(userId);
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }
        return result;
    }
}