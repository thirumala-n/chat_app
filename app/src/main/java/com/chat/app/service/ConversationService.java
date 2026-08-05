package com.chat.app.service;

import com.chat.app.dto.request.CreateGroupRequest;
import com.chat.app.dto.response.ConversationResponse;
import com.chat.app.entity.Conversation;
import com.chat.app.entity.ConversationMember;
import com.chat.app.entity.Message;
import com.chat.app.entity.User;
import com.chat.app.enums.ConversationType;
import com.chat.app.enums.MemberRole;
import com.chat.app.exception.BadRequestException;
import com.chat.app.exception.ForbiddenException;
import com.chat.app.exception.ResourceNotFoundException;
import com.chat.app.mapper.ChatMapper;
import com.chat.app.repository.ConversationMemberRepository;
import com.chat.app.repository.ConversationRepository;
import com.chat.app.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChatMapper chatMapper;

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(String userId, boolean archived) {
        List<Conversation> conversations = archived
                ? conversationRepository.findArchivedByUserId(userId)
                : conversationRepository.findActiveByUserId(userId);

        return conversations.stream()
                .map(conv -> toResponseWithMemberContext(conv, userId))
                .toList();
    }

    @Transactional
    public ConversationResponse getOrCreateDirectConversation(String userId, String otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("Cannot create conversation with yourself");
        }
        userService.findUser(otherUserId);

        return conversationRepository.findDirectConversation(userId, otherUserId, ConversationType.DIRECT)
                .map(conv -> toResponseWithMemberContext(conv, userId))
                .orElseGet(() -> createDirectConversation(userId, otherUserId));
    }

    @Transactional
    public ConversationResponse createGroup(String userId, CreateGroupRequest request) {
        User creator = userService.findUser(userId);

        Conversation conversation = Conversation.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(ConversationType.GROUP)
                .createdBy(creator)
                .build();

        conversation = conversationRepository.save(conversation);

        addMember(conversation, creator, MemberRole.ADMIN);

        if (request.getMemberIds() != null) {
            for (String memberId : request.getMemberIds()) {
                if (!memberId.equals(userId)) {
                    User member = userService.findUser(memberId);
                    addMember(conversation, member, MemberRole.MEMBER);
                }
            }
        }

        return toResponseWithMemberContext(
                conversationRepository.findByIdWithMembers(conversation.getId()).orElse(conversation),
                userId);
    }

    @Transactional
    public ConversationResponse addMembers(String userId, String conversationId, List<String> memberIds) {
        Conversation conversation = findConversationWithAccess(userId, conversationId);
        if (conversation.getType() != ConversationType.GROUP) {
            throw new BadRequestException("Can only add members to group conversations");
        }

        for (String memberId : memberIds) {
            if (!memberRepository.existsByConversationIdAndUserId(conversationId, memberId)) {
                User member = userService.findUser(memberId);
                addMember(conversation, member, MemberRole.MEMBER);
            }
        }

        return toResponseWithMemberContext(
                conversationRepository.findByIdWithMembers(conversationId).orElse(conversation),
                userId);
    }

    @Transactional
    public void removeMember(String userId, String conversationId, String memberId) {
        Conversation conversation = findConversationWithAccess(userId, conversationId);
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getRole() == MemberRole.ADMIN && !memberId.equals(userId)) {
            throw new ForbiddenException("Only admins can remove other members");
        }

        memberRepository.delete(member);
    }

    @Transactional
    public ConversationResponse togglePin(String userId, String conversationId) {
        ConversationMember member = getMember(userId, conversationId);
        member.setPinned(!member.isPinned());
        memberRepository.save(member);
        return toResponseWithMemberContext(findConversationWithAccess(userId, conversationId), userId);
    }

    @Transactional
    public ConversationResponse toggleArchive(String userId, String conversationId) {
        ConversationMember member = getMember(userId, conversationId);
        member.setArchived(!member.isArchived());
        memberRepository.save(member);
        return toResponseWithMemberContext(findConversationWithAccess(userId, conversationId), userId);
    }

    @Transactional
    public void deleteConversation(String userId, String conversationId) {
        ConversationMember member = getMember(userId, conversationId);
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> searchConversations(String userId, String query) {
        return conversationRepository.searchByUserAndName(userId, query).stream()
                .map(conv -> toResponseWithMemberContext(conv, userId))
                .toList();
    }

    public Conversation findConversationWithAccess(String userId, String conversationId) {
        Conversation conversation = conversationRepository.findByIdWithMembers(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new ForbiddenException("Not a member of this conversation");
        }
        return conversation;
    }

    private ConversationResponse createDirectConversation(String userId, String otherUserId) {
        User currentUser = userService.findUser(userId);
        User otherUser = userService.findUser(otherUserId);

        Conversation conversation = Conversation.builder()
                .name(otherUser.getFullName())
                .type(ConversationType.DIRECT)
                .createdBy(currentUser)
                .build();

        conversation = conversationRepository.save(conversation);
        addMember(conversation, currentUser, MemberRole.MEMBER);
        addMember(conversation, otherUser, MemberRole.MEMBER);

        return toResponseWithMemberContext(
                conversationRepository.findByIdWithMembers(conversation.getId()).orElse(conversation),
                userId);
    }

    private void addMember(Conversation conversation, User user, MemberRole role) {
        ConversationMember member = ConversationMember.builder()
                .conversation(conversation)
                .user(user)
                .role(role)
                .build();
        memberRepository.save(member);
    }

    private ConversationMember getMember(String userId, String conversationId) {
        return memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member of this conversation"));
    }

    private ConversationResponse toResponseWithMemberContext(Conversation conversation, String userId) {
        ConversationResponse response = chatMapper.toConversationResponse(conversation);
        response.setMembers(conversation.getMembers().stream()
                .map(chatMapper::toMemberResponse)
                .toList());

        memberRepository.findByConversationIdAndUserId(conversation.getId(), userId)
                .ifPresent(member -> {
                    response.setPinned(member.isPinned());
                    response.setArchived(member.isArchived());
                });

        messageRepository.findByConversationId(conversation.getId(), PageRequest.of(0, 1))
                .getContent().stream().findFirst()
                .ifPresent(msg -> response.setLastMessage(chatMapper.toMessageResponse(msg)));

        return response;
    }
}
