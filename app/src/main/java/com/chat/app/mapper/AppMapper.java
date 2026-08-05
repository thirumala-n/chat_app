package com.chat.app.mapper;

import com.chat.app.dto.response.AiConversationResponse;
import com.chat.app.dto.response.AiMessageResponse;
import com.chat.app.dto.response.NotificationResponse;
import com.chat.app.entity.AiConversation;
import com.chat.app.entity.AiMessage;
import com.chat.app.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AppMapper {

    @Mapping(target = "type", expression = "java(notification.getType().name())")
    @Mapping(target = "sender", source = "sender")
    NotificationResponse toNotificationResponse(Notification notification);

    @Mapping(target = "provider", expression = "java(conversation.getProvider().name())")
    AiConversationResponse toAiConversationResponse(AiConversation conversation);

    AiMessageResponse toAiMessageResponse(AiMessage message);
}
