package com.chat.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReactionRequest {

    @NotBlank(message = "Emoji is required")
    private String emoji;
}
