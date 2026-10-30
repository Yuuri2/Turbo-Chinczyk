package com.turbochinczyk.backend.message;

public record CommandAcknowledgedMessage(
        String requestId,
        String command) implements LobbyMessage {
}
