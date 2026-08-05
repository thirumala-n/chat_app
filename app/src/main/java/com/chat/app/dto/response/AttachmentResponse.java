package com.chat.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private String id;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private long fileSize;
    private String thumbnailUrl;
}
