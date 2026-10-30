package com.turbochinczyk.backend.message;

public record SetReadyCommand(
        String requestId,
        boolean ready) implements LobbyMessage {

    public SetReadyCommand(boolean ready) {
        this(null, ready);
    }
}
