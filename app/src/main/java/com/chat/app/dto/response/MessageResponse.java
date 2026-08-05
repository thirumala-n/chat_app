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
public class MessageResponse {
    private String id;
    private String conversationId;
    private UserResponse sender;
    private String content;
    private String type;
    private String status;
    private MessageResponse replyTo;
    private MessageResponse forwardedFrom;
    private boolean edited;
    private boolean deleted;
    private List<AttachmentResponse> attachments;
    private List<ReactionResponse> reactions;
    private String mentionedUserIds;
    private Instant createdAt;
    private Instant updatedAt;
}
