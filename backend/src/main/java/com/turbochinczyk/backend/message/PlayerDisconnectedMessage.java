package com.turbochinczyk.backend.message;

public record PlayerDisconnectedMessage(int userId, String username) implements LobbyMessage {}