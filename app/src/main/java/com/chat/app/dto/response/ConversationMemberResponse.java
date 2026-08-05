package com.chat.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberResponse {
    private String id;
    private UserResponse user;
    private String role;
    private boolean pinned;
    private boolean archived;
}
