package com.turbochinczyk.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import com.turbochinczyk.backend.database.AuthService;


@Component
public class LobbyHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final AuthService authService;
    private final LobbyManager lobbyManager;

    public LobbyHandshakeInterceptor(
            AuthService authService,
            LobbyManager lobbyManager) {
        this.authService = authService;
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String path = request.getURI().getPath();
        String[] pathSegments = path.split("/");
        if (pathSegments.length != 3 || pathSegments[1].isBlank() || pathSegments[2].isBlank()) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        int lobbyId;
        try {
            lobbyId = Integer.parseInt(pathSegments[pathSegments.length - 1]);
        } catch (NumberFormatException exception) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        Lobby lobby = lobbyManager.get(lobbyId);
        if (lobby == null) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return false;
        }

        HttpServletRequest servletRequest =
                ((ServletServerHttpRequest) request).getServletRequest();
        String token = findSessionCookie(servletRequest);
        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        var user = authService.validateToken(token);
        if(user.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (!lobby.hasMember(user.get().getUserId())) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put("LOBBY_ID", lobbyId);
        attributes.put("USER", user.get());

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    private String findSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("session".equals(cookie.getName())
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        return null;
    }
    
}
