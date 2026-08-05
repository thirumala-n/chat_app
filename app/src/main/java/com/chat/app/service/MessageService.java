package com.chat.app.service;

import com.chat.app.dto.request.EditMessageRequest;
import com.chat.app.dto.request.ReactionRequest;
import com.chat.app.dto.request.SendMessageRequest;
import com.chat.app.dto.response.MessageResponse;
import com.chat.app.entity.Attachment;
import com.chat.app.entity.Conversation;
import com.chat.app.entity.ConversationMember;
import com.chat.app.entity.Message;
import com.chat.app.entity.MessageReaction;
import com.chat.app.entity.User;
import com.chat.app.enums.MessageStatus;
import com.chat.app.enums.MessageType;
import com.chat.app.enums.NotificationType;
import com.chat.app.exception.BadRequestException;
import com.chat.app.exception.ForbiddenException;
import com.chat.app.exception.ResourceNotFoundException;
import com.chat.app.mapper.ChatMapper;
import com.chat.app.repository.ConversationMemberRepository;
import com.chat.app.repository.MessageReactionRepository;
import com.chat.app.repository.MessageRepository;
import com.chat.app.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageReactionRepository reactionRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationService conversationService;
    private final UserService userService;
    private final ChatMapper chatMapper;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final WebSocketEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(String userId, String conversationId, int page, int size) {
        conversationService.findConversationWithAccess(userId, conversationId);
        return messageRepository.findByConversationId(conversationId, PageRequest.of(page, size))
                .map(chatMapper::toMessageResponse);
    }

    @Transactional
    public MessageResponse sendMessage(String userId, SendMessageRequest request) {
        Conversation conversation = conversationService.findConversationWithAccess(userId, request.getConversationId());
        User sender = userService.findUser(userId);

        MessageType type = request.getType() != null
                ? MessageType.valueOf(request.getType()) : MessageType.TEXT;

        Message.MessageBuilder builder = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .type(type)
                .status(MessageStatus.SENT)
                .mentionedUserIds(request.getMentionedUserIds());

        if (request.getReplyToId() != null) {
            Message replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reply message not found"));
            builder.replyTo(replyTo);
        }

        if (request.getForwardedFromId() != null) {
            Message forwarded = messageRepository.findById(request.getForwardedFromId())
                    .orElseThrow(() -> new ResourceNotFoundException("Forwarded message not found"));
            builder.forwardedFrom(forwarded);
        }

        Message message = messageRepository.save(builder.build());
        MessageResponse response = chatMapper.toMessageResponse(
                messageRepository.findByIdWithDetails(message.getId()).orElse(message));

        eventPublisher.publishMessage(conversation.getId(), response);
        notifyMembers(conversation, sender, message);
        return response;
    }

    @Transactional
    public MessageResponse sendMessageWithAttachments(String userId, String conversationId,
                                                    String content, String type,
                                                    List<MultipartFile> files) {
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conversationId);
        request.setContent(content);
        request.setType(type != null ? type : determineMessageType(files).name());

        MessageResponse response = sendMessage(userId, request);
        Message message = messageRepository.findById(response.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = fileStorageService.storeFile(file, "messages");
            Attachment attachment = Attachment.builder()
                    .message(message)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();
            attachments.add(attachment);
        }
        message.getAttachments().addAll(attachments);
        messageRepository.save(message);

        return chatMapper.toMessageResponse(messageRepository.findByIdWithDetails(message.getId()).orElse(message));
    }

    @Transactional
    public MessageResponse editMessage(String userId, String messageId, EditMessageRequest request) {
        Message message = findMessage(messageId);
        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException("Cannot edit another user's message");
        }
        message.setContent(request.getContent());
        message.setEdited(true);
        Message saved = messageRepository.save(message);
        MessageResponse response = chatMapper.toMessageResponse(saved);
        eventPublisher.publishMessageUpdate(message.getConversation().getId(), response);
        return response;
    }

    @Transactional
    public void deleteMessage(String userId, String messageId) {
        Message message = findMessage(messageId);
        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException("Cannot delete another user's message");
        }
        message.setDeleted(true);
        message.setContent("This message was deleted");
        messageRepository.save(message);
        eventPublisher.publishMessageDelete(message.getConversation().getId(), messageId);
    }

    @Transactional
    public MessageResponse addReaction(String userId, String messageId, ReactionRequest request) {
        Message message = findMessage(messageId);
        User user = userService.findUser(userId);

        reactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, userId, request.getEmoji())
                .ifPresent(reactionRepository::delete);

        MessageReaction reaction = MessageReaction.builder()
                .message(message)
                .user(user)
                .emoji(request.getEmoji())
                .build();
        reactionRepository.save(reaction);

        MessageResponse response = chatMapper.toMessageResponse(
                messageRepository.findByIdWithDetails(messageId).orElse(message));
        eventPublisher.publishMessageUpdate(message.getConversation().getId(), response);
        return response;
    }

    @Transactional
    public void markAsRead(String userId, String conversationId, String messageId) {
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member"));
        member.setLastReadMessageId(messageId);
        memberRepository.save(member);

        eventPublisher.publishReadReceipt(conversationId, userId, messageId);
    }

    @Transactional
    public MessageResponse forwardMessage(String userId, String messageId, String targetConversationId) {
        Message original = findMessage(messageId);
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(targetConversationId);
        request.setContent(original.getContent());
        request.setType(original.getType().name());
        request.setForwardedFromId(messageId);
        return sendMessage(userId, request);
    }

    public List<String> getMessageSuggestions(String conversationId, String partialMessage) {
        return List.of(
                "Thanks for letting me know!",
                "Sounds good to me 👍",
                "I'll get back to you on this.",
                "Can we discuss this later?",
                "Great idea!"
        );
    }

    private Message findMessage(String messageId) {
        return messageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    private MessageType determineMessageType(List<MultipartFile> files) {
        if (files.isEmpty()) return MessageType.TEXT;
        String contentType = files.get(0).getContentType();
        if (contentType == null) return MessageType.DOCUMENT;
        if (contentType.startsWith("image/")) return MessageType.IMAGE;
        if (contentType.startsWith("video/")) return MessageType.VIDEO;
        if (contentType.startsWith("audio/")) return MessageType.AUDIO;
        if (contentType.equals("application/pdf")) return MessageType.PDF;
        return MessageType.DOCUMENT;
    }

    private void notifyMembers(Conversation conversation, User sender, Message message) {
        for (ConversationMember member : conversation.getMembers()) {
            if (!member.getUser().getId().equals(sender.getId())) {
                notificationService.createNotification(
                        member.getUser(),
                        sender,
                        NotificationType.MESSAGE,
                        "New message from " + sender.getFullName(),
                        message.getContent(),
                        message.getId());
            }
        }
    }
}
