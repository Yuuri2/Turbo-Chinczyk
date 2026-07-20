package com.turbochinczyk.backend;

import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.PlayerJoinedMessage;
import com.turbochinczyk.backend.message.PlayerReconnectedMessage;
import com.turbochinczyk.backend.message.PlayerDisconnectedMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String lobbyId;
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();

    public Lobby(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public void handlePlayerJoin(int userId, String username, WebSocketSession session) {
        if (players.containsKey(userId)) {
            Player existingPlayer = players.get(userId);
            existingPlayer.updateSession(session);
            broadcast(new PlayerReconnectedMessage(userId, existingPlayer.getUsername()));
        } else {
            Player newPlayer = new Player(userId, username, session);
            players.put(userId, newPlayer);
            broadcast(new PlayerJoinedMessage(userId, username));
        }
    }

    public void handlePlayerDisconnect(int userId) {
        Player player = players.get(userId);
        if (player != null) {
            player.setOffline();
            broadcast(new PlayerDisconnectedMessage(userId, player.getUsername()));
        }
    }

    public void handlePlayerMessage(WebSocketSession session, LobbyMessage message){
        System.out.printf("W in the chat: %s\n", message.toString());
    }

    public void broadcast(LobbyMessage message) {
        // writeValueAsString w Jackson 3.x rzuca unchecked JacksonException,
        // więc nie trzeba już try/catch tylko na potrzeby kompilacji
        String json = MAPPER.writeValueAsString(message);

        players.values().forEach(player -> {
            try {
                player.sendNetworkMessage(json);
            } catch (IOException e) {
                // Zerwane połączenie, obsłużone przy heartbeat/disconnect
            }
        });
    }
}