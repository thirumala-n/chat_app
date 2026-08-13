package com.chat.app.websocket;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserPresenceChangedEvent extends ApplicationEvent {

    private final String userId;
    private final String status; // "ONLINE" or "OFFLINE"

    public UserPresenceChangedEvent(Object source, String userId, String status) {
        super(source);
        this.userId = userId;
        this.status = status;
    }
}