package com.turbochinczyk.backend.message;

public record PlayerLeftMessage(int userId, String username) implements LobbyMessage {}
