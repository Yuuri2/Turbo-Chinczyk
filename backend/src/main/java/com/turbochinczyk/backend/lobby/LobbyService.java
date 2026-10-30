package com.turbochinczyk.backend.lobby;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.LobbyManager;
import com.turbochinczyk.backend.database.UserSessionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LobbyService {
    private final LobbyManager lobbyManager;

    public LobbyService(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    public List<Lobby.LobbySnapshot> listJoinableLobbies() {
        return lobbyManager.listLobbies()
                .stream()
                .filter(Lobby::isJoinable)
                .map(Lobby::snapshot)
                .toList();
    }

    public Lobby.LobbySnapshot createLobby(
            String name,
            UserSessionDto user,
            String password,
            int maxPlayers) {
        Lobby lobby = lobbyManager.createLobby(
                name,
                user.getUserId(),
                password,
                maxPlayers);
        lobby.addMember(user.getUserId(), user.getUsername());
        return lobby.snapshot();
    }

    public Lobby getLobby(int lobbyId) {
        Lobby lobby = lobbyManager.get(lobbyId);
        if (lobby == null) {
            throw new LobbyNotFoundException();
        }
        return lobby;
    }

    public Lobby.LobbySnapshot joinLobby(
            int lobbyId,
            UserSessionDto user,
            String password) {
        Lobby lobby = getLobby(lobbyId);
        if (!lobby.hasCorrectPassword(password)) {
            throw new IncorrectLobbyPasswordException();
        }
        lobby.addMember(user.getUserId(), user.getUsername());
        return lobby.snapshot();
    }

    public void leaveLobby(int lobbyId, int userId) {
        Lobby lobby = getLobby(lobbyId);
        if (!lobby.removeMember(userId)) {
            throw new LobbyMemberNotFoundException();
        }
        if (lobby.getPlayerCount() == 0) {
            lobbyManager.deleteLobby(lobbyId);
        }
    }

    public void deleteLobby(int lobbyId, int userId) {
        Lobby lobby = getLobby(lobbyId);
        if (!lobby.isHost(userId)) {
            throw new LobbyAccessDeniedException();
        }
        lobbyManager.deleteLobby(lobbyId);
    }

    public Lobby.LobbySnapshot setReady(
            int lobbyId,
            int userId,
            boolean ready) {
        Lobby lobby = getLobby(lobbyId);
        lobby.setPlayerReady(userId, ready);
        return lobby.snapshot();
    }

    public Lobby.LobbySnapshot startGame(int lobbyId, int userId) {
        Lobby lobby = getLobby(lobbyId);
        lobby.startGame(userId);
        return lobby.snapshot();
    }

    public Lobby.LobbySnapshot finishGame(int lobbyId, int userId) {
        Lobby lobby = getLobby(lobbyId);
        lobby.finishGame(userId);
        return lobby.snapshot();
    }

    public static class LobbyNotFoundException extends RuntimeException {
    }

    public static class LobbyMemberNotFoundException extends RuntimeException {
    }

    public static class IncorrectLobbyPasswordException extends RuntimeException {
    }

    public static class LobbyAccessDeniedException extends RuntimeException {
    }
}
