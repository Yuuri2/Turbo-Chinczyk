package com.turbochinczyk.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import com.turbochinczyk.backend.database.AuthService;


@Component
public class LobbyHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    
    @Autowired
    private AuthService authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String path = request.getURI().getPath();
        String[] pathSegments = path.split("/");
        if(pathSegments.length <= 2) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        String lobbyId = pathSegments[pathSegments.length - 1];

        // TODO: zmienić to na sprawdzanie ciasteczek zamiast tokena w linku jak ngnix będzie działać
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = servletRequest.getParameter("token");

        if(token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        var user = authService.validateToken(token);
        if(user.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // TODO: sprawdzić czy user id jest w lobby id puzniej jak zrobi się zapytanie do bazy
        boolean isAllowed = true;
        if(!isAllowed) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put("LOBBY_ID", lobbyId);
        attributes.put("USER", user.get());

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
    
}
