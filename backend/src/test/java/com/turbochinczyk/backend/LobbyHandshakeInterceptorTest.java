package com.turbochinczyk.backend;

import com.turbochinczyk.backend.database.AuthService;
import com.turbochinczyk.backend.database.UserSessionDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LobbyHandshakeInterceptorTest {

    @Test
    void validCookieAndMemberAreStoredInHandshakeAttributes() throws Exception {
        LobbyManager manager = new LobbyManager();
        Lobby lobby = manager.createLobby("Test", 7, null, 4);
        lobby.addMember(7, "Alice");
        AuthService authService = mock(AuthService.class);
        when(authService.validateToken("valid-token")).thenReturn(
                java.util.Optional.of(new UserSessionDto(7, "Alice")));

        LobbyHandshakeInterceptor interceptor =
                new LobbyHandshakeInterceptor(authService, manager);
        MockHttpServletRequest servletRequest = request("/game/" + lobby.getLobbyId());
        servletRequest.setCookies(
                new jakarta.servlet.http.Cookie("session", "valid-token"));
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class),
                attributes);

        assertTrue(accepted);
        assertEquals(lobby.getLobbyId(), attributes.get("LOBBY_ID"));
        assertEquals(7, ((UserSessionDto) attributes.get("USER")).getUserId());
    }

    @Test
    void nonNumericLobbyIdIsRejected() throws Exception {
        AuthService authService = mock(AuthService.class);
        LobbyHandshakeInterceptor interceptor =
                new LobbyHandshakeInterceptor(authService, new LobbyManager());
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean accepted = interceptor.beforeHandshake(
                new ServletServerHttpRequest(request("/game/not-a-number")),
                response,
                mock(WebSocketHandler.class),
                new HashMap<>());

        assertFalse(accepted);
        verify(response).setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        request.setServletPath(path);
        return request;
    }
}
