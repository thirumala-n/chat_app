package com.chat.app.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final WebSocketEventPublisher eventPublisher;

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null) return;
        String conversationId = (String) payload.get("conversationId");
        boolean typing = Boolean.TRUE.equals(payload.get("typing"));
        String username = payload.get("username") != null ? payload.get("username").toString() : "User";
        eventPublisher.publishTyping(conversationId, principal.getName(), username, typing);
    }
}
