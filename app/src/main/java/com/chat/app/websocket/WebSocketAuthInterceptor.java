package com.chat.app.websocket;

import com.chat.app.security.JwtService;
import com.chat.app.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final OnlineUserService onlineUserService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT rejected: Missing or invalid Authorization header");
                throw new IllegalArgumentException("Unauthorized: Missing or invalid Authorization header");
            }

            String token = authToken.substring(7).trim();
            try {
                if (jwtService.isTokenExpired(token)) {
                    log.warn("WebSocket CONNECT rejected: JWT token expired");
                    throw new IllegalArgumentException("Unauthorized: JWT token expired");
                }

                String userId = jwtService.extractUserId(token);
                if (userId == null || userId.isBlank()) {
                    log.warn("WebSocket CONNECT rejected: Missing userId in JWT claims");
                    throw new IllegalArgumentException("Unauthorized: Invalid token claims");
                }

                accessor.setUser(() -> userId);
                onlineUserService.setUserOnline(userId);
                applicationEventPublisher.publishEvent(
                        new UserPresenceChangedEvent(this, userId, "ONLINE"));
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                log.warn("WebSocket CONNECT authentication failed: {}", e.getClass().getSimpleName());
                throw new IllegalArgumentException("Unauthorized: Authentication failed");
            }
        }

        return message;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String userId = accessor.getUser().getName();
            onlineUserService.setUserOffline(userId);
            applicationEventPublisher.publishEvent(
                    new UserPresenceChangedEvent(this, userId, "OFFLINE"));
        }
    }
}