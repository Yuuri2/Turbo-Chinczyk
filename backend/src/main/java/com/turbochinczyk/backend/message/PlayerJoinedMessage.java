package com.turbochinczyk.backend.message;

public record PlayerJoinedMessage(int userId, String username) implements LobbyMessage {}