package com.chat.app.repository;

import com.chat.app.entity.Conversation;
import com.chat.app.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT DISTINCT c FROM Conversation c JOIN FETCH c.members m JOIN FETCH m.user " +
            "WHERE m.user.id = :userId AND m.archived = false ORDER BY c.updatedAt DESC")
    List<Conversation> findActiveByUserId(@Param("userId") String userId);

    @Query("SELECT DISTINCT c FROM Conversation c JOIN FETCH c.members m JOIN FETCH m.user " +
            "WHERE m.user.id = :userId AND m.archived = true ORDER BY c.updatedAt DESC")
    List<Conversation> findArchivedByUserId(@Param("userId") String userId);

    @Query("SELECT c FROM Conversation c JOIN c.members m1 JOIN c.members m2 " +
            "WHERE c.type = :type AND m1.user.id = :userId1 AND m2.user.id = :userId2")
    Optional<Conversation> findDirectConversation(
            @Param("userId1") String userId1,
            @Param("userId2") String userId2,
            @Param("type") ConversationType type);

    @Query("SELECT DISTINCT c FROM Conversation c JOIN FETCH c.members m JOIN FETCH m.user " +
            "WHERE m.user.id = :userId AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Conversation> searchByUserAndName(@Param("userId") String userId, @Param("query") String query);

    @Query("SELECT c FROM Conversation c JOIN FETCH c.members m JOIN FETCH m.user WHERE c.id = :id")
    Optional<Conversation> findByIdWithMembers(@Param("id") String id);
}
