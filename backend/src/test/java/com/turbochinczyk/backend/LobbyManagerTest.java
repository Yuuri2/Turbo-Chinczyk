package com.turbochinczyk.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyManagerTest {
    private final LobbyManager lobbyManager = new LobbyManager();

    @AfterEach
    void clearLobbies() {
        lobbyManager.clear();
    }

    @Test
    void createLobbyStoresExplicitMetadata() {
        Lobby lobby = lobbyManager.createLobby("Test lobby", 7, "secret", 4);

        assertNotNull(lobbyManager.get(lobby.getLobbyId()));
        assertEquals("Test lobby", lobby.getName());
        assertEquals(7, lobby.getHostId());
        assertTrue(lobby.isPasswordProtected());
        assertEquals(4, lobby.getMaxPlayers());
    }

    @Test
    void createLobbyGeneratesDistinctIds() {
        Lobby first = lobbyManager.createLobby("First", 1, null, 4);
        Lobby second = lobbyManager.createLobby("Second", 2, null, 4);

        assertNotEquals(first.getLobbyId(), second.getLobbyId());
        assertEquals(2, lobbyManager.listLobbies().size());
    }

    @Test
    void deleteLobbyRemovesOnlyRequestedLobby() {
        Lobby retained = lobbyManager.createLobby("Retained", 1, null, 4);
        Lobby deleted = lobbyManager.createLobby("Deleted", 2, null, 4);

        assertTrue(lobbyManager.deleteLobby(deleted.getLobbyId()));

        assertNotNull(lobbyManager.get(retained.getLobbyId()));
        assertNull(lobbyManager.get(deleted.getLobbyId()));
    }

    @Test
    void deletingUnknownLobbyReportsNoRemoval() {
        assertFalse(lobbyManager.deleteLobby(999));
    }
}
