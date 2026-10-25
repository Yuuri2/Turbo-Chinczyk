package com.turbochinczyk.backend;

import com.turbochinczyk.backend.message.LobbyMessage;
import com.turbochinczyk.backend.message.PlayerJoinedMessage;
import com.turbochinczyk.backend.message.PlayerLeftMessage;
import com.turbochinczyk.backend.message.LobbySnapshotMessage;
import com.turbochinczyk.backend.message.PlayerReadyMessage;
import com.turbochinczyk.backend.message.PlayerReconnectedMessage;
import com.turbochinczyk.backend.message.PlayerDisconnectedMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final int DEFAULT_MAX_PLAYERS = 4;

    private final String lobbyId;
    private final String name;
    private int hostId;
    private final String password;
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, WebSocketSession> connections =
            new ConcurrentHashMap<>();
    private final int maxPlayers;
    private LobbyStatus status;

    public Lobby(String lobbyId) {
        this(lobbyId, "Lobby", 0, null, DEFAULT_MAX_PLAYERS);
    }

    public Lobby(String lobbyId, int maxPlayers) {
        this(lobbyId, "Lobby", 0, null, maxPlayers);
    }

    public Lobby(
            String lobbyId,
            String name,
            int hostId,
            String password,
            int maxPlayers) {
        if (lobbyId == null || lobbyId.isBlank()) {
            throw new IllegalArgumentException("Lobby ID must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Lobby name must not be blank");
        }
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("Lobby capacity must be positive");
        }

        this.lobbyId = lobbyId;
        this.name = name;
        this.hostId = hostId;
        this.password = normalizePassword(password);
        this.maxPlayers = maxPlayers;
        this.status = LobbyStatus.WAITING;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getName() {
        return name;
    }

    public int getHostId() {
        return hostId;
    }

    public boolean isHost(int userId) {
        return hostId == userId;
    }

    public Optional<Player> getHost() {
        return Optional.ofNullable(players.get(hostId));
    }

    public boolean isPasswordProtected() {
        return password != null;
    }

    public boolean hasCorrectPassword(String candidatePassword) {
        return !isPasswordProtected()
                || (candidatePassword != null && password.equals(candidatePassword));
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean hasMember(int userId) {
        return players.containsKey(userId);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public boolean isJoinable() {
        return status == LobbyStatus.WAITING && getPlayerCount() < maxPlayers;
    }

    public synchronized void startGame(int userId) {
        requireHost(userId);
        if (status != LobbyStatus.WAITING) {
            throw new IllegalStateException("Lobby is not waiting to start");
        }
        if (players.isEmpty()) {
            throw new IllegalStateException("Lobby must have at least one player");
        }
        if (players.values().stream().anyMatch(player -> !player.isReady())) {
            throw new IllegalStateException("All players must be ready");
        }
        status = LobbyStatus.PLAYING;
    }

    public synchronized void finishGame(int userId) {
        requireHost(userId);
        if (status != LobbyStatus.PLAYING) {
            throw new IllegalStateException("Lobby is not currently playing");
        }
        status = LobbyStatus.FINISHED;
    }

    public synchronized LobbySnapshot snapshot() {
        List<PlayerSnapshot> playerSnapshots = players.values()
                .stream()
                .sorted(Comparator.comparingInt(Player::getUserId))
                .map(player -> new PlayerSnapshot(
                        player.getUserId(),
                        player.getUsername(),
                        connections.containsKey(player.getUserId()),
                        player.isReady()))
                .toList();

        return new LobbySnapshot(
                lobbyId,
                name,
                hostId,
                status,
                maxPlayers,
                isPasswordProtected(),
                playerSnapshots);
    }

    public synchronized void handlePlayerJoin(
            int userId,
            String username,
            WebSocketSession session) {
        Player existingPlayer = players.get(userId);

        if (existingPlayer != null) {
            connectPlayer(userId, session);
            broadcast(new PlayerReconnectedMessage(userId, existingPlayer.getUsername()));
            return;
        }

        if (!isJoinable()) {
            throw new IllegalStateException("Lobby is full or no longer accepting players");
        }

        Player newPlayer = new Player(userId, username);
        players.put(userId, newPlayer);
        connectPlayer(userId, session);
        broadcast(new PlayerJoinedMessage(userId, username));
    }

    public synchronized void handlePlayerDisconnect(int userId, String sessionId) {
        disconnectPlayer(userId, sessionId);
    }

    public synchronized void addMember(int userId, String username) {
        if (players.containsKey(userId)) {
            return;
        }
        if (!isJoinable()) {
            throw new IllegalStateException("Lobby is full or no longer accepting players");
        }
        players.put(userId, new Player(userId, username));
        broadcast(new PlayerJoinedMessage(userId, username));
    }

    public synchronized void connectPlayer(int userId, WebSocketSession session) {
        if (session == null) {
            throw new IllegalArgumentException("WebSocket session must not be null");
        }
        if (!players.containsKey(userId)) {
            throw new IllegalStateException("User is not a member of this lobby");
        }
        connections.put(userId, session);
    }

    public synchronized void disconnectPlayer(int userId, String sessionId) {
        Player player = players.get(userId);
        WebSocketSession currentSession = connections.get(userId);
        if (player != null
                && currentSession != null
                && currentSession.getId().equals(sessionId)
                && connections.remove(userId, currentSession)) {
            broadcast(new PlayerDisconnectedMessage(userId, player.getUsername()));
        }
    }

    public synchronized boolean removeMember(int userId) {
        Player removedPlayer = players.remove(userId);
        if (removedPlayer == null) {
            return false;
        }

        connections.remove(userId);
        if (userId == hostId && !players.isEmpty()) {
            hostId = players.keySet().stream().min(Integer::compareTo).orElseThrow();
        }
        broadcast(new PlayerLeftMessage(
                userId,
                removedPlayer.getUsername()));
        return true;
    }

    public synchronized void setPlayerReady(int userId, boolean ready) {
        Player player = players.get(userId);
        if (player == null) {
            throw new IllegalStateException("User is not a member of this lobby");
        }
        if (status != LobbyStatus.WAITING) {
            throw new IllegalStateException("Lobby is no longer waiting");
        }
        player.setReady(ready);
        broadcast(new PlayerReadyMessage(
                userId,
                player.getUsername(),
                ready));
    }

    private void requireHost(int userId) {
        if (!isHost(userId)) {
            throw new IllegalStateException("Only the host can change lobby status");
        }
    }

    public void handlePlayerMessage(WebSocketSession session, LobbyMessage message){
        System.out.printf("W in the chat: %s\n", message.toString());
    }

    public synchronized void sendSnapshot(WebSocketSession session) throws IOException {
        String json = MAPPER.writeValueAsString(
                new LobbySnapshotMessage(snapshot()));
        if (session.isOpen()) {
            session.sendMessage(new org.springframework.web.socket.TextMessage(json));
        }
    }

    public void broadcast(LobbyMessage message) {
        // writeValueAsString w Jackson 3.x rzuca unchecked JacksonException,
        // więc nie trzeba już try/catch tylko na potrzeby kompilacji
        String json = MAPPER.writeValueAsString(message);

        connections.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(json));
                }
            } catch (IOException e) {
                // Zerwane połączenie, obsłużone przy heartbeat/disconnect
            }
        });
    }

    public enum LobbyStatus {
        WAITING,
        PLAYING,
        FINISHED
    }

    public record LobbySnapshot(
            String lobbyId,
            String name,
            int hostId,
            LobbyStatus status,
            int maxPlayers,
            boolean passwordProtected,
            List<PlayerSnapshot> players) {
    }

    public record PlayerSnapshot(
            int userId,
            String username,
            boolean online,
            boolean ready) {
    }

    private static String normalizePassword(String password) {
        if (password == null || password.isBlank()) {
            return null;
        }
        return password;
    }
}