package com.turbochinczyk.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyMembershipTest {

    @Test
    void leavingHostTransfersHostToLowestRemainingUserId() {
        Lobby lobby = new Lobby(1, "Test lobby", 10, null, 4);
        lobby.addMember(10, "Host");
        lobby.addMember(20, "Bob");
        lobby.addMember(15, "Alice");

        assertTrue(lobby.removeMember(10));

        assertEquals(15, lobby.getHostId());
        assertFalse(lobby.hasMember(10));
        assertEquals(2, lobby.getPlayerCount());
    }

    @Test
    void removingLastMemberLeavesEmptyLobby() {
        Lobby lobby = new Lobby(1, "Test lobby", 10, null, 4);
        lobby.addMember(10, "Host");

        assertTrue(lobby.removeMember(10));

        assertEquals(0, lobby.getPlayerCount());
        assertFalse(lobby.hasMember(10));
    }
}
