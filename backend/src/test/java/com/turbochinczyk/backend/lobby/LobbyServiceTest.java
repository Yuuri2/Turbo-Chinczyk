package com.turbochinczyk.backend.lobby;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.LobbyManager;
import com.turbochinczyk.backend.database.UserSessionDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LobbyServiceTest {

    @Test
    void nonMemberCannotChangeReadiness() {
        LobbyService service = serviceWithLobby(1, 4);

        assertThrows(
                IllegalStateException.class,
                () -> service.setReady(1, 2, true));
    }

    @Test
    void nonHostCannotDeleteLobby() {
        LobbyService service = serviceWithLobby(1, 4);

        assertThrows(
                LobbyService.LobbyAccessDeniedException.class,
                () -> service.deleteLobby(1, 2));
    }

    @Test
    void fullLobbyRejectsAnotherMember() {
        LobbyManager manager = new LobbyManager();
        LobbyService service = new LobbyService(manager);
        UserSessionDto host = new UserSessionDto(1, "Alice");
        service.createLobby("Test lobby", host, null, 1);

        assertThrows(
                IllegalStateException.class,
                () -> service.joinLobby(
                        1,
                        new UserSessionDto(2, "Bob"),
                        null));
    }

    @Test
    void leavingLobbyTransfersHostToRemainingMember() {
        LobbyManager manager = new LobbyManager();
        LobbyService service = new LobbyService(manager);
        service.createLobby(
                "Test lobby",
                new UserSessionDto(1, "Alice"),
                null,
                4);
        service.joinLobby(
                1,
                new UserSessionDto(2, "Bob"),
                null);

        service.leaveLobby(1, 1);

        assertTrue(service.getLobby(1).isHost(2));
    }

    private LobbyService serviceWithLobby(int hostId, int maxPlayers) {
        LobbyManager manager = new LobbyManager();
        LobbyService service = new LobbyService(manager);
        service.createLobby(
                "Test lobby",
                new UserSessionDto(hostId, "Alice"),
                null,
                maxPlayers);
        return service;
    }
}
