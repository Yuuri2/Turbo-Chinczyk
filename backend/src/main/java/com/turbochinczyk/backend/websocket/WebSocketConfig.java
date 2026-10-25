package com.turbochinczyk.backend.websocket;

import com.turbochinczyk.backend.LobbyHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;
    private final LobbyHandshakeInterceptor lobbyHandshakeInterceptor;

    public WebSocketConfig(
            GameWebSocketHandler gameWebSocketHandler,
            LobbyHandshakeInterceptor lobbyHandshakeInterceptor) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.lobbyHandshakeInterceptor = lobbyHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/game/{lobbyId}")
                .addInterceptors(lobbyHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}