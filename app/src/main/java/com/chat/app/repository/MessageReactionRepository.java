package com.chat.app.repository;

import com.chat.app.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {

    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(String messageId, String userId, String emoji);

    List<MessageReaction> findByMessageId(String messageId);

    void deleteByMessageIdAndUserIdAndEmoji(String messageId, String userId, String emoji);
}
