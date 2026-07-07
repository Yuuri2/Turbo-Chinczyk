package com.turbochinczyk.backend;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    private final String lobbyId;
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();

    public Lobby(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public void handlePlayerJoin(int userId, String username, org.springframework.web.socket.WebSocketSession session) {
        if (players.containsKey(userId)) {
            // Reconnection scenario
            Player existingPlayer = players.get(userId);
            existingPlayer.updateSession(session);
            broadcast(existingPlayer.getUsername() + " reconnected!");
        } else {
            // Brand new player joining
            Player newPlayer = new Player(userId, username, session);
            players.put(userId, newPlayer);
            broadcast(newPlayer.getUsername() + " joined the lobby!");
        }
    }

    public void handlePlayerDisconnect(int userId) {
        Player player = players.get(userId);
        if (player != null) {
            player.setOffline();
            broadcast(player.getUsername() + " disconnected. Waiting for reconnection...");
            
            // Optional: Start a separate thread/timer to remove them permanently 
            // from the map after 10-15 seconds if they don't reconnect.
        }
    }

    public void broadcast(String message) {
        players.values().forEach(player -> {
            try {
                player.sendNetworkMessage(message);
            } catch (IOException e) {
                // Connection failed silently, handled during heartbeat or manual disconnect
            }
        });
    }
}