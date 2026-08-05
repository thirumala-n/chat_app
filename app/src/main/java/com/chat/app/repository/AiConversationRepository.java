package com.chat.app.repository;

import com.chat.app.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, String> {

    List<AiConversation> findByUserIdOrderByUpdatedAtDesc(String userId);

    @Query("SELECT c FROM AiConversation c LEFT JOIN FETCH c.messages WHERE c.id = :id AND c.user.id = :userId")
    Optional<AiConversation> findByIdAndUserIdWithMessages(String id, String userId);
}
