package com.chat.app.service;

import com.chat.app.dto.request.ChangePasswordRequest;
import com.chat.app.dto.request.UpdateProfileRequest;
import com.chat.app.dto.response.UserResponse;
import com.chat.app.entity.User;
import com.chat.app.enums.UserStatus;
import com.chat.app.exception.BadRequestException;
import com.chat.app.exception.ResourceNotFoundException;
import com.chat.app.mapper.UserMapper;
import com.chat.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final OnlineUserService onlineUserService;

    @Transactional(readOnly = true)
    public UserResponse getProfile(String userId) {
        return userMapper.toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getStatus() != null) {
            user.setStatus(UserStatus.valueOf(request.getStatus()));
            onlineUserService.updateStatus(userId, request.getStatus());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadProfileImage(String userId, MultipartFile file) {
        User user = findUser(userId);
        String fileUrl = fileStorageService.storeFile(file, "profiles");
        user.setProfileImageUrl(fileUrl);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (user.getPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query, String currentUserId) {
        return userRepository.searchUsers(query, currentUserId).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public void updateLastSeen(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastSeenAt(Instant.now());
            userRepository.save(user);
        });
    }

    public User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
