package com.turbochinczyk.backend;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyManager {
    private final ConcurrentHashMap<Integer, Lobby> activeLobbies =
            new ConcurrentHashMap<>();
    private final AtomicInteger nextLobbyId = new AtomicInteger(1);

    public Lobby createLobby(
            String name,
            int hostId,
            String password,
            int maxPlayers) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Lobby name must not be blank");
        }
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("Lobby capacity must be positive");
        }

        int lobbyId = nextLobbyId.getAndIncrement();
        Lobby lobby = new Lobby(
                lobbyId,
                name,
                hostId,
                password,
                maxPlayers);
        activeLobbies.put(lobbyId, lobby);
        return lobby;
    }

    public Lobby get(int lobbyId) {
        return activeLobbies.get(lobbyId);
    }

    public List<Lobby> listLobbies() {
        return List.copyOf(activeLobbies.values());
    }

    public boolean deleteLobby(int lobbyId) {
        return activeLobbies.remove(lobbyId) != null;
    }

    public void clear() {
        activeLobbies.clear();
    }
}