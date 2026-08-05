package com.chat.app.service;

import com.chat.app.dto.response.NotificationResponse;
import com.chat.app.entity.Notification;
import com.chat.app.entity.User;
import com.chat.app.enums.NotificationType;
import com.chat.app.mapper.AppMapper;
import com.chat.app.repository.NotificationRepository;
import com.chat.app.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppMapper appMapper;
    private final WebSocketEventPublisher eventPublisher;

    @Transactional
    public Notification createNotification(User recipient, User sender, NotificationType type,
                                           String title, String body, String referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .type(type)
                .title(title)
                .body(body)
                .referenceId(referenceId)
                .build();

        notification = notificationRepository.save(notification);
        NotificationResponse response = appMapper.toNotificationResponse(notification);
        eventPublisher.publishNotification(recipient.getId(), response);
        return notification;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String userId, int page, int size) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(appMapper::toNotificationResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(String userId, String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getRecipient().getId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
