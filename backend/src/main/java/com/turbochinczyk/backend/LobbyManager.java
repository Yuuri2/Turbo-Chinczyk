package com.turbochinczyk.backend;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyManager {
    // Keeps track of all active game rooms on the server
    private final ConcurrentHashMap<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

    public Lobby getOrCreateLobby(String lobbyId) {
        return activeLobbies.computeIfAbsent(lobbyId, id -> new Lobby(id));
    }
    
    public Lobby get(String lobbyId) {
        return activeLobbies.get(lobbyId);
    }

    public void deleteLobby(String lobbyId) {
        activeLobbies.remove(lobbyId);
    }
}