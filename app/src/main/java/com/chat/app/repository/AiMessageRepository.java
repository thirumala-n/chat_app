package com.chat.app.repository;

import com.chat.app.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, String> {
    List<AiMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}
