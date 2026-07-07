package com.turbochinczyk.backend.message;

public record PlayerReconnectedMessage(int userId, String username) implements LobbyMessage {}