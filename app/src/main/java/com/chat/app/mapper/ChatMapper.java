package com.chat.app.mapper;

import com.chat.app.dto.response.AttachmentResponse;
import com.chat.app.dto.response.ConversationMemberResponse;
import com.chat.app.dto.response.ConversationResponse;
import com.chat.app.dto.response.MessageResponse;
import com.chat.app.dto.response.ReactionResponse;
import com.chat.app.entity.Attachment;
import com.chat.app.entity.Conversation;
import com.chat.app.entity.ConversationMember;
import com.chat.app.entity.Message;
import com.chat.app.entity.MessageReaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ChatMapper {

    @Mapping(target = "type", expression = "java(conversation.getType().name())")
    @Mapping(target = "pinned", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "lastMessage", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    ConversationResponse toConversationResponse(Conversation conversation);

    @Mapping(target = "role", expression = "java(member.getRole().name())")
    ConversationMemberResponse toMemberResponse(ConversationMember member);

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "type", expression = "java(message.getType().name())")
    @Mapping(target = "status", expression = "java(message.getStatus().name())")
    @Mapping(target = "replyTo", source = "replyTo")
    @Mapping(target = "forwardedFrom", source = "forwardedFrom")
    MessageResponse toMessageResponse(Message message);

    AttachmentResponse toAttachmentResponse(Attachment attachment);

    @Mapping(target = "user", source = "user")
    ReactionResponse toReactionResponse(MessageReaction reaction);

    List<ConversationMemberResponse> toMemberResponses(List<ConversationMember> members);
}
