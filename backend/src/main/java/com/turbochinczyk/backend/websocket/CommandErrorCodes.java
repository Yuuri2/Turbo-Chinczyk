package com.turbochinczyk.backend.websocket;

public final class CommandErrorCodes {
    public static final String MALFORMED_MESSAGE = "MALFORMED_MESSAGE";
    public static final String UNSUPPORTED_COMMAND = "UNSUPPORTED_COMMAND";
    public static final String LOBBY_RULE_VIOLATION = "LOBBY_RULE_VIOLATION";

    private CommandErrorCodes() {
    }
}
