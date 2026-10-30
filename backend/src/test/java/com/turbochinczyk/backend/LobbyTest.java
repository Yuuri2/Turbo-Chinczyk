package com.turbochinczyk.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyTest {

    @Test
    void newLobbyStartsWaitingAndHasNoMembers() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);

        assertEquals(Lobby.LobbyStatus.WAITING, lobby.getStatus());
        assertEquals(0, lobby.getPlayerCount());
        assertTrue(lobby.isJoinable());
    }

    @Test
    void membersCanJoinUntilLobbyReachesCapacity() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 2);

        lobby.addMember(1, "Alice");
        lobby.addMember(2, "Bob");

        assertEquals(2, lobby.getPlayerCount());
        assertFalse(lobby.isJoinable());
    }

    @Test
    void joiningFullLobbyIsRejected() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 1);
        lobby.addMember(1, "Alice");

        assertThrows(
                IllegalStateException.class,
                () -> lobby.addMember(2, "Bob"));
    }

    @Test
    void joiningExistingMemberIsIdempotent() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 2);
        lobby.addMember(1, "Alice");

        lobby.addMember(1, "Alice");

        assertEquals(1, lobby.getPlayerCount());
        assertTrue(lobby.hasMember(1));
    }

    @Test
    void snapshotReportsHttpCreatedMembersAsOffline() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 2);
        lobby.addMember(1, "Alice");

        Lobby.PlayerSnapshot player = lobby.snapshot().players().getFirst();

        assertEquals(1, player.userId());
        assertEquals("Alice", player.username());
        assertFalse(player.online());
    }

    @Test
    void passwordIsKeptPrivateAndValidatedInMemory() {
        Lobby lobby = new Lobby(1, "Private lobby", 1, "secret", 4);

        assertTrue(lobby.isPasswordProtected());
        assertTrue(lobby.hasCorrectPassword("secret"));
        assertFalse(lobby.hasCorrectPassword("wrong"));
        assertFalse(lobby.snapshot().toString().contains("secret"));
    }

    @Test
    void blankPasswordCreatesPublicLobby() {
        Lobby lobby = new Lobby(1, "Public lobby", 1, "  ", 4);

        assertFalse(lobby.isPasswordProtected());
        assertTrue(lobby.hasCorrectPassword(null));
    }

    @Test
    void hostCanStartAndFinishGame() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");
        lobby.setPlayerReady(1, true);

        lobby.startGame(1);
        assertEquals(Lobby.LobbyStatus.PLAYING, lobby.getStatus());

        lobby.finishGame(1);
        assertEquals(Lobby.LobbyStatus.FINISHED, lobby.getStatus());
    }

    @Test
    void nonHostCannotStartGame() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");

        assertThrows(
                IllegalStateException.class,
                () -> lobby.startGame(2));
    }

    @Test
    void nonHostCannotFinishGame() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");
        lobby.setPlayerReady(1, true);
        lobby.startGame(1);

        assertThrows(
                IllegalStateException.class,
                () -> lobby.finishGame(2));
    }

    @Test
    void allMembersMustBeReadyBeforeStarting() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");
        lobby.addMember(2, "Bob");

        assertThrows(
                IllegalStateException.class,
                () -> lobby.startGame(1));

        lobby.setPlayerReady(1, true);
        lobby.setPlayerReady(2, true);
        lobby.startGame(1);

        assertEquals(Lobby.LobbyStatus.PLAYING, lobby.getStatus());
    }

    @Test
    void readinessIsIncludedInSnapshot() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");
        lobby.setPlayerReady(1, true);

        assertTrue(lobby.snapshot().players().getFirst().ready());
    }

    @Test
    void snapshotsCanReportConnectedMembers() {
        Lobby lobby = new Lobby(1, "Test lobby", 1, null, 4);
        lobby.addMember(1, "Alice");

        assertTrue(lobby.snapshot(java.util.Set.of(1)).players().getFirst().online());
        assertFalse(lobby.snapshot().players().getFirst().online());
    }
}
