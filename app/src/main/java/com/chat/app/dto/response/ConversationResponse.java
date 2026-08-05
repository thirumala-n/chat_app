package com.chat.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private String name;
    private String description;
    private String avatarUrl;
    private String type;
    private boolean pinned;
    private boolean archived;
    private UserResponse createdBy;
    private List<ConversationMemberResponse> members;
    private MessageResponse lastMessage;
    private long unreadCount;
    private Instant createdAt;
    private Instant updatedAt;
}
