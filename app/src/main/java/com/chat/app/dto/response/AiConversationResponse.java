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
public class AiConversationResponse {
    private String id;
    private String title;
    private String provider;
    private String featureType;
    private List<AiMessageResponse> messages;
    private Instant createdAt;
    private Instant updatedAt;
}
