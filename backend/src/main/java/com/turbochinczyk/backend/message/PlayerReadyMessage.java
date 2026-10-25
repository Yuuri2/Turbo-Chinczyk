package com.turbochinczyk.backend.message;

public record PlayerReadyMessage(
        int userId,
        String username,
        boolean ready) implements LobbyMessage {}
