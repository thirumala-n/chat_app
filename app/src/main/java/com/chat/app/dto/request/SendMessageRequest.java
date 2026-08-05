package com.chat.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    private String content;
    private String type;
    private String replyToId;
    private String forwardedFromId;
    private String mentionedUserIds;
}
