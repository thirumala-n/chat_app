package com.chat.app.repository;

import com.chat.app.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, String> {

    Optional<ConversationMember> findByConversationIdAndUserId(String conversationId, String userId);

    List<ConversationMember> findByConversationId(String conversationId);

    boolean existsByConversationIdAndUserId(String conversationId, String userId);
}
