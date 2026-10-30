package com.turbochinczyk.backend.message;

public record StartGameCommand(String requestId) implements LobbyMessage {
}
