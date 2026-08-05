package com.chat.app.controller;

import com.chat.app.dto.request.AiChatRequest;
import com.chat.app.dto.response.AiConversationResponse;
import com.chat.app.dto.response.AiMessageResponse;
import com.chat.app.dto.response.ApiResponse;
import com.chat.app.security.SecurityUtils;
import com.chat.app.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Spring AI powered assistant")
public class AiController {

    private final AiChatService aiChatService;
    private final SecurityUtils securityUtils;

    @GetMapping("/conversations")
    @Operation(summary = "Get AI conversations")
    public ResponseEntity<ApiResponse<List<AiConversationResponse>>> getConversations() {
        return ResponseEntity.ok(ApiResponse.success(
                aiChatService.getConversations(securityUtils.getCurrentUserId())));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get AI conversation with messages")
    public ResponseEntity<ApiResponse<AiConversationResponse>> getConversation(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                aiChatService.getConversation(securityUtils.getCurrentUserId(), id)));
    }

    @PostMapping("/chat")
    @Operation(summary = "Send message to AI assistant")
    public ResponseEntity<ApiResponse<AiMessageResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                aiChatService.chat(securityUtils.getCurrentUserId(), request)));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream AI response")
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(120000L);
        Flux<String> stream = aiChatService.streamChat(securityUtils.getCurrentUserId(), request);

        stream.subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );

        return emitter;
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete AI conversation")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String id) {
        aiChatService.deleteConversation(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }
}
