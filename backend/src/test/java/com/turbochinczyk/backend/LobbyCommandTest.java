package com.turbochinczyk.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyCommandTest {

    @Test
    void memberCanSetReadyThroughTheDomainModel() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");

        lobby.setPlayerReady(1, true);

        assertTrue(lobby.snapshot().players().getFirst().ready());
    }
}
