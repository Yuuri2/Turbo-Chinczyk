package com.turbochinczyk.backend;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArrayList;

public class GameWebSocketHandler extends TextWebSocketHandler {

    // Thread-safe list to keep track of active player sessions
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Player connected! Session ID: " + session.getId());
        session.sendMessage(new TextMessage("Welcome to the game server!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received from client (" + session.getId() + "): " + payload);
        
        // Broadcast the message to all other connected players
        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(new TextMessage("Broadcast: " + payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Player disconnected: " + session.getId());
    }
}