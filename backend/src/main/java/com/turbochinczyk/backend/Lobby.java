package com.turbochinczyk.backend;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    public static final int DEFAULT_MAX_PLAYERS = 4;

    private final int lobbyId;
    private final String name;
    private int hostId;
    private final String password;
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    private final int maxPlayers;
    private LobbyStatus status;

    public Lobby(int lobbyId) {
        this(lobbyId, "Lobby", 0, null, DEFAULT_MAX_PLAYERS);
    }

    public Lobby(int lobbyId, int maxPlayers) {
        this(lobbyId, "Lobby", 0, null, maxPlayers);
    }

    public Lobby(
            int lobbyId,
            String name,
            int hostId,
            String password,
            int maxPlayers) {
        if (lobbyId <= 0) {
            throw new IllegalArgumentException("Lobby ID must be positive");
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

    public int getLobbyId() {
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

    public Optional<Player> getMember(int userId) {
        return Optional.ofNullable(players.get(userId));
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
        return snapshot(Set.of());
    }

    public synchronized LobbySnapshot snapshot(Set<Integer> connectedUserIds) {
        List<PlayerSnapshot> playerSnapshots = players.values()
                .stream()
                .sorted(Comparator.comparingInt(Player::getUserId))
                .map(player -> new PlayerSnapshot(
                        player.getUserId(),
                        player.getUsername(),
                        connectedUserIds.contains(player.getUserId()),
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

    public synchronized void addMember(int userId, String username) {
        if (players.containsKey(userId)) {
            return;
        }
        if (!isJoinable()) {
            throw new IllegalStateException("Lobby is full or no longer accepting players");
        }
        players.put(userId, new Player(userId, username));
    }

    public synchronized boolean removeMember(int userId) {
        Player removedPlayer = players.remove(userId);
        if (removedPlayer == null) {
            return false;
        }

        if (userId == hostId && !players.isEmpty()) {
            hostId = players.keySet().stream().min(Integer::compareTo).orElseThrow();
        }
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
    }

    private void requireHost(int userId) {
        if (!isHost(userId)) {
            throw new IllegalStateException("Only the host can change lobby status");
        }
    }

    public enum LobbyStatus {
        WAITING,
        PLAYING,
        FINISHED
    }

    public record LobbySnapshot(
            int lobbyId,
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