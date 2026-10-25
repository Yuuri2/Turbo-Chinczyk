package com.turbochinczyk.backend.message;

import com.turbochinczyk.backend.Lobby;

public record LobbySnapshotMessage(Lobby.LobbySnapshot lobby) implements LobbyMessage {}
