package com.turbochinczyk.backend;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyManager {
    private final ConcurrentHashMap<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

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

        String lobbyId = UUID.randomUUID().toString();
        Lobby lobby = new Lobby(
                lobbyId,
                name,
                hostId,
                password,
                maxPlayers);
        activeLobbies.put(lobbyId, lobby);
        return lobby;
    }

    public Lobby get(String lobbyId) {
        return activeLobbies.get(lobbyId);
    }

    public List<Lobby> listLobbies() {
        return List.copyOf(activeLobbies.values());
    }

    public boolean deleteLobby(String lobbyId) {
        return activeLobbies.remove(lobbyId) != null;
    }

    public DeleteLobbyResult deleteLobby(String lobbyId, int userId) {
        Lobby lobby = activeLobbies.get(lobbyId);
        if (lobby == null) {
            return DeleteLobbyResult.NOT_FOUND;
        }
        synchronized (lobby) {
            if (!lobby.isHost(userId)) {
                return DeleteLobbyResult.NOT_HOST;
            }
            activeLobbies.remove(lobbyId, lobby);
            return DeleteLobbyResult.DELETED;
        }
    }

    public boolean removeMember(String lobbyId, int userId) {
        Lobby lobby = activeLobbies.get(lobbyId);
        if (lobby == null || !lobby.removeMember(userId)) {
            return false;
        }
        if (lobby.getPlayerCount() == 0) {
            activeLobbies.remove(lobbyId, lobby);
        }
        return true;
    }

    public LobbyStatusResult startGame(String lobbyId, int userId) {
        Lobby lobby = activeLobbies.get(lobbyId);
        if (lobby == null) {
            return LobbyStatusResult.NOT_FOUND;
        }
        try {
            lobby.startGame(userId);
            return LobbyStatusResult.UPDATED;
        } catch (IllegalStateException exception) {
            return LobbyStatusResult.REJECTED;
        }
    }

    public LobbyStatusResult setPlayerReady(
            String lobbyId,
            int userId,
            boolean ready) {
        Lobby lobby = activeLobbies.get(lobbyId);
        if (lobby == null) {
            return LobbyStatusResult.NOT_FOUND;
        }
        try {
            lobby.setPlayerReady(userId, ready);
            return LobbyStatusResult.UPDATED;
        } catch (IllegalStateException exception) {
            return LobbyStatusResult.REJECTED;
        }
    }

    public LobbyStatusResult finishGame(String lobbyId, int userId) {
        Lobby lobby = activeLobbies.get(lobbyId);
        if (lobby == null) {
            return LobbyStatusResult.NOT_FOUND;
        }
        try {
            lobby.finishGame(userId);
            return LobbyStatusResult.UPDATED;
        } catch (IllegalStateException exception) {
            return LobbyStatusResult.REJECTED;
        }
    }

    public void clear() {
        activeLobbies.clear();
    }

    public enum DeleteLobbyResult {
        DELETED,
        NOT_FOUND,
        NOT_HOST
    }

    public enum LobbyStatusResult {
        UPDATED,
        NOT_FOUND,
        REJECTED
    }
}