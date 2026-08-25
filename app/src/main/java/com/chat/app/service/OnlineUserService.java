package com.chat.app.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, String> userStatuses = new ConcurrentHashMap<>();

    public void setUserOnline(String userId) {
        if (userId != null) {
            onlineUsers.add(userId);
            userStatuses.put(userId, "ONLINE");
        }
    }

    public void setUserOffline(String userId) {
        if (userId != null) {
            onlineUsers.remove(userId);
            userStatuses.put(userId, "OFFLINE");
        }
    }

    public void updateStatus(String userId, String status) {
        if (userId == null) {
            return;
        }
        String effectiveStatus = (status != null && !status.isBlank()) ? status : "ONLINE";
        userStatuses.put(userId, effectiveStatus);
        if ("ONLINE".equalsIgnoreCase(effectiveStatus)) {
            onlineUsers.add(userId);
        } else {
            onlineUsers.remove(userId);
        }
    }

    public boolean isUserOnline(String userId) {
        return userId != null && onlineUsers.contains(userId);
    }

    public Set<String> getOnlineUserIds() {
        return Collections.unmodifiableSet(onlineUsers);
    }

    public Map<String, String> getOnlineUsersMap() {
        return new HashMap<>(userStatuses);
    }
}
