package com.turbochinczyk.backend;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player {
    private final int userId;
    private final String username;
    private WebSocketSession session;
    private boolean isOnline;

    public Player(int userId, String username, WebSocketSession session) {
        this.userId = userId;
        this.username = username;
        this.session = session;
        this.isOnline = true;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isOnline() { return isOnline; }
    
    public void updateSession(WebSocketSession newSession) {
        this.session = newSession;
        this.isOnline = true;
    }
    
    public void setOffline() {
        this.isOnline = false;
        this.session = null;
    }

    public void sendNetworkMessage(String payload) throws IOException {
        if (isOnline && session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(payload));
        }
    }
}
