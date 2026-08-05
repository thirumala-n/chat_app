package com.chat.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String bio;
    private String profileImageUrl;
    private String status;
    private Instant lastSeenAt;
    private boolean emailVerified;
    private Set<String> roles;
    private Instant createdAt;
}
