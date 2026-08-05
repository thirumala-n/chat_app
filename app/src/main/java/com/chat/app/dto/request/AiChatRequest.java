package com.chat.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequest {

    private String conversationId;

    @NotBlank(message = "Message is required")
    private String message;

    private String provider;
    private String featureType;
}
