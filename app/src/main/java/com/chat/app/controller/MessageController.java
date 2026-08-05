package com.chat.app.controller;

import com.chat.app.dto.request.EditMessageRequest;
import com.chat.app.dto.request.ReactionRequest;
import com.chat.app.dto.request.SendMessageRequest;
import com.chat.app.dto.response.ApiResponse;
import com.chat.app.dto.response.MessageResponse;
import com.chat.app.security.SecurityUtils;
import com.chat.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Message management")
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtils securityUtils;

    @GetMapping("/conversation/{conversationId}")
    @Operation(summary = "Get messages for conversation")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessages(securityUtils.getCurrentUserId(), conversationId, page, size)));
    }

    @PostMapping
    @Operation(summary = "Send a message")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.sendMessage(securityUtils.getCurrentUserId(), request)));
    }

    @PostMapping(value = "/with-attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Send message with attachments")
    public ResponseEntity<ApiResponse<MessageResponse>> sendWithAttachments(
            @RequestParam String conversationId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String type,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.sendMessageWithAttachments(
                        securityUtils.getCurrentUserId(), conversationId, content, type, files)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a message")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable String id, @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.editMessage(securityUtils.getCurrentUserId(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    @PostMapping("/{id}/reactions")
    @Operation(summary = "Add/toggle reaction")
    public ResponseEntity<ApiResponse<MessageResponse>> addReaction(
            @PathVariable String id, @Valid @RequestBody ReactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.addReaction(securityUtils.getCurrentUserId(), id, request)));
    }

    @PostMapping("/{id}/forward")
    @Operation(summary = "Forward message")
    public ResponseEntity<ApiResponse<MessageResponse>> forwardMessage(
            @PathVariable String id, @RequestParam String targetConversationId) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.forwardMessage(securityUtils.getCurrentUserId(), id, targetConversationId)));
    }

    @PostMapping("/read")
    @Operation(summary = "Mark messages as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestParam String conversationId, @RequestParam String messageId) {
        messageService.markAsRead(securityUtils.getCurrentUserId(), conversationId, messageId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get AI message suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(
            @RequestParam String conversationId, @RequestParam String partial) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessageSuggestions(conversationId, partial)));
    }
}
