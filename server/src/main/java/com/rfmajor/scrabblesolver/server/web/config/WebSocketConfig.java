package com.rfmajor.scrabblesolver.server.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rfmajor.scrabblesolver.server.web.websocket.MessageProcessor;
import com.rfmajor.scrabblesolver.server.web.websocket.WebSocketMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final List<MessageProcessor> messageProcessors;
    private final Map<String, List<String>> activeRooms;
    private final ObjectMapper objectMapper;

    @Bean
    public Map<String, WebSocketSession> sessionRegistry() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                new WebSocketMessageHandler(sessionRegistry(), activeRooms, messageProcessors, objectMapper),
                        "/test")
                .setAllowedOrigins("*");
    }
}
