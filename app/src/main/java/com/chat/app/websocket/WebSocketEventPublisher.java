package com.chat.app.websocket;

import com.chat.app.dto.response.MessageResponse;
import com.chat.app.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishMessage(String conversationId, MessageResponse message) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
    }

    public void publishMessageUpdate(String conversationId, MessageResponse message) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/update", message);
    }

    public void publishMessageDelete(String conversationId, String messageId) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/delete",
                (Object) Map.of("messageId", messageId));
    }

    public void publishTyping(String conversationId, String userId, String username, boolean typing) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/typing",
                (Object) Map.of("userId", userId, "username", username, "typing", typing));
    }

    public void publishReadReceipt(String conversationId, String userId, String messageId) {
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/read",
                (Object) Map.of("userId", userId, "messageId", messageId));
    }

    public void publishNotification(String userId, NotificationResponse notification) {
        messagingTemplate.convertAndSend("/queue/notifications/" + userId, notification);
    }

    public void publishOnlineStatus(String userId, String status) {
        messagingTemplate.convertAndSend("/topic/presence", (Object) Map.of("userId", userId, "status", status));
    }

    @EventListener
    public void onUserPresenceChanged(UserPresenceChangedEvent event) {
        publishOnlineStatus(event.getUserId(), event.getStatus());
    }
}