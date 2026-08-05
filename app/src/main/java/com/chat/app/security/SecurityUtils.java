package com.chat.app.security;

import com.chat.app.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final CustomUserDetailsService userDetailsService;

    public SecurityUtils(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.chat.app.exception.UnauthorizedException("Not authenticated");
        }
        return userDetailsService.loadUserEntityByEmail(authentication.getName());
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
