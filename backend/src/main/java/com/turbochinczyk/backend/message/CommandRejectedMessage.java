package com.turbochinczyk.backend.message;

public record CommandRejectedMessage(
        String requestId,
        String command,
        String code,
        String message) implements LobbyMessage {
}
