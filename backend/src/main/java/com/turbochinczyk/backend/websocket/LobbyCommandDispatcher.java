package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.lobby.LobbyService;
import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.CommandAcknowledgedMessage;
import com.turbochinczyk.backend.message.CommandRejectedMessage;
import com.turbochinczyk.backend.message.LobbySnapshotMessage;
import com.turbochinczyk.backend.message.PlayerReadyMessage;
import com.turbochinczyk.backend.message.SetReadyCommand;
import com.turbochinczyk.backend.message.StartGameCommand;
import org.springframework.stereotype.Component;

@Component
public class LobbyCommandDispatcher {
    private final LobbyService lobbyService;
    private final LobbyConnectionManager connectionManager;

    public LobbyCommandDispatcher(
            LobbyService lobbyService,
            LobbyConnectionManager connectionManager) {
        this.lobbyService = lobbyService;
        this.connectionManager = connectionManager;
    }

    public void dispatch(
            int lobbyId,
            String sessionId,
            LobbyMessage message) throws java.io.IOException {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        int userId = connectionManager.getUserId(lobbyId, sessionId);
        if (userId < 0) {
            throw new IllegalStateException(
                    "WebSocket session is not registered for a lobby member");
        }

        if (message instanceof SetReadyCommand command) {
            try {
                lobbyService.setReady(lobbyId, userId, command.ready());
                connectionManager.broadcast(
                        lobbyId,
                        new PlayerReadyMessage(
                                userId,
                                lobby.getMember(userId).orElseThrow().getUsername(),
                                command.ready()));
                acknowledge(lobbyId, userId, command.requestId(), "SET_READY");
            } catch (IllegalStateException exception) {
                reject(
                        lobbyId,
                        userId,
                        command.requestId(),
                        "SET_READY",
                        CommandErrorCodes.LOBBY_RULE_VIOLATION,
                        exception.getMessage());
            }
            return;
        }

        if (message instanceof StartGameCommand command) {
            try {
                lobbyService.startGame(lobbyId, userId);
                connectionManager.broadcast(
                        lobbyId,
                        new LobbySnapshotMessage(
                                lobby.snapshot(
                                        connectionManager.connectedUserIds(lobbyId))));
                acknowledge(lobbyId, userId, command.requestId(), "START_GAME");
            } catch (IllegalStateException exception) {
                reject(
                        lobbyId,
                        userId,
                        command.requestId(),
                        "START_GAME",
                        CommandErrorCodes.LOBBY_RULE_VIOLATION,
                        exception.getMessage());
            }
            return;
        }

        throw new IllegalArgumentException(
                "Unsupported lobby message: "
                        + message.getClass().getSimpleName());
    }

    public int getUserId(int lobbyId, String sessionId) {
        return connectionManager.getUserId(lobbyId, sessionId);
    }

    public void sendRejection(
            int lobbyId,
            int userId,
            CommandRejectedMessage rejection) throws java.io.IOException {
        connectionManager.sendTo(lobbyId, userId, rejection);
    }

    private void acknowledge(
            int lobbyId,
            int userId,
            String requestId,
            String command) throws java.io.IOException {
        connectionManager.sendTo(
                        lobbyId,
                        userId,
                        new CommandAcknowledgedMessage(requestId, command));
    }

    private void reject(
            int lobbyId,
            int userId,
            String requestId,
            String command,
            String code,
            String message) throws java.io.IOException {
        connectionManager.sendTo(
                        lobbyId,
                        userId,
                        new CommandRejectedMessage(requestId, command, code, message));
    }
}
