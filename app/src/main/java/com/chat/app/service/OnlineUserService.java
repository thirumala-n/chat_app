package com.chat.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private static final String ONLINE_USERS_KEY = "online:users";
    private static final String USER_STATUS_PREFIX = "user:status:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void setUserOnline(String userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
        redisTemplate.opsForValue().set(USER_STATUS_PREFIX + userId, "ONLINE", Duration.ofHours(24));
    }

    public void setUserOffline(String userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
        redisTemplate.opsForValue().set(USER_STATUS_PREFIX + userId, "OFFLINE", Duration.ofDays(7));
    }

    public void updateStatus(String userId, String status) {
        redisTemplate.opsForValue().set(USER_STATUS_PREFIX + userId, status, Duration.ofHours(24));
        if ("ONLINE".equals(status)) {
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
        } else {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
        }
    }

    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId));
    }

    public Set<String> getOnlineUserIds() {
        Set<Object> members = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        if (members == null) {
            return Set.of();
        }
        return members.stream().map(Object::toString).collect(Collectors.toSet());
    }

    public Map<String, String> getOnlineUsersMap() {
        Set<String> onlineIds = getOnlineUserIds();
        Map<String, String> statusMap = new HashMap<>();
        for (String userId : onlineIds) {
            Object status = redisTemplate.opsForValue().get(USER_STATUS_PREFIX + userId);
            statusMap.put(userId, status != null ? status.toString() : "ONLINE");
        }
        return statusMap;
    }
}
