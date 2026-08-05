package com.chat.app.controller;

import com.chat.app.dto.request.ChangePasswordRequest;
import com.chat.app.dto.request.UpdateProfileRequest;
import com.chat.app.dto.response.ApiResponse;
import com.chat.app.dto.response.UserResponse;
import com.chat.app.security.SecurityUtils;
import com.chat.app.service.OnlineUserService;
import com.chat.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;
    private final OnlineUserService onlineUserService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(securityUtils.getCurrentUserId())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(securityUtils.getCurrentUserId(), request)));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile image")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.uploadProfileImage(securityUtils.getCurrentUserId(), file)));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(securityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.searchUsers(q, securityUtils.getCurrentUserId())));
    }

    @GetMapping("/online")
    @Operation(summary = "Get online users")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOnlineUsers() {
        return ResponseEntity.ok(ApiResponse.success(onlineUserService.getOnlineUsersMap()));
    }

    @GetMapping("/{userId}/online")
    @Operation(summary = "Check if user is online")
    public ResponseEntity<ApiResponse<Boolean>> isUserOnline(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(onlineUserService.isUserOnline(userId)));
    }
}
