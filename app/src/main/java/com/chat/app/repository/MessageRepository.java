package com.chat.app.repository;

import com.chat.app.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.sender LEFT JOIN FETCH m.attachments " +
            "LEFT JOIN FETCH m.reactions r LEFT JOIN FETCH r.user " +
            "WHERE m.conversation.id = :conversationId AND m.deleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByConversationId(@Param("conversationId") String conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.sender LEFT JOIN FETCH m.attachments " +
            "LEFT JOIN FETCH m.reactions r LEFT JOIN FETCH r.user WHERE m.id = :id")
    Optional<Message> findByIdWithDetails(@Param("id") String id);

    long countByConversationIdAndDeletedFalse(String conversationId);
}
