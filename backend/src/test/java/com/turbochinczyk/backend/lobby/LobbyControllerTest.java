package com.turbochinczyk.backend.lobby;

import com.turbochinczyk.backend.Lobby;
import com.turbochinczyk.backend.database.AuthService;
import com.turbochinczyk.backend.database.UserSessionDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LobbyControllerTest {

    @Test
    void createLobbyUsesTheAuthenticatedCookieUser() {
        LobbyService service = mock(LobbyService.class);
        AuthService authService = mock(AuthService.class);
        LobbyController controller = new LobbyController(service, authService);
        UserSessionDto user = new UserSessionDto(7, "Alice");
        Lobby.LobbySnapshot snapshot = new Lobby.LobbySnapshot(
                1,
                "Test lobby",
                7,
                Lobby.LobbyStatus.WAITING,
                4,
                false,
                List.of());

        when(authService.validateToken("valid-token")).thenReturn(
                java.util.Optional.of(user));
        when(service.createLobby(
                "Test lobby",
                user,
                null,
                4)).thenReturn(snapshot);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("session", "valid-token"));

        var response = controller.createLobby(
                new LobbyController.CreateLobbyRequest("Test lobby", null, 4),
                request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(snapshot, response.getBody());
    }

    @Test
    void listLobbiesDoesNotRequireAuthentication() {
        LobbyController controller =
                new LobbyController(mock(LobbyService.class), mock(AuthService.class));

        assertEquals(List.of(), controller.listLobbies());
    }
}
