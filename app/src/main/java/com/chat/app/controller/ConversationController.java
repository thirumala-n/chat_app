package com.chat.app.controller;

import com.chat.app.dto.request.CreateGroupRequest;
import com.chat.app.dto.response.ApiResponse;
import com.chat.app.dto.response.ConversationResponse;
import com.chat.app.security.SecurityUtils;
import com.chat.app.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Chat conversation management")
public class ConversationController {

    private final ConversationService conversationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get user conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getUserConversations(securityUtils.getCurrentUserId(), archived)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.searchConversations(securityUtils.getCurrentUserId(), q)));
    }

    @PostMapping("/direct/{userId}")
    @Operation(summary = "Get or create direct conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreateDirect(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getOrCreateDirectConversation(securityUtils.getCurrentUserId(), userId)));
    }

    @PostMapping("/groups")
    @Operation(summary = "Create group conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> createGroup(
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.createGroup(securityUtils.getCurrentUserId(), request)));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add members to group")
    public ResponseEntity<ApiResponse<ConversationResponse>> addMembers(
            @PathVariable String id, @RequestBody List<String> memberIds) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.addMembers(securityUtils.getCurrentUserId(), id, memberIds)));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove member from group")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable String id, @PathVariable String memberId) {
        conversationService.removeMember(securityUtils.getCurrentUserId(), id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }

    @PutMapping("/{id}/pin")
    @Operation(summary = "Toggle pin conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> togglePin(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.togglePin(securityUtils.getCurrentUserId(), id)));
    }

    @PutMapping("/{id}/archive")
    @Operation(summary = "Toggle archive conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> toggleArchive(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.toggleArchive(securityUtils.getCurrentUserId(), id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete/leave conversation")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String id) {
        conversationService.deleteConversation(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Conversation removed", null));
    }
}
