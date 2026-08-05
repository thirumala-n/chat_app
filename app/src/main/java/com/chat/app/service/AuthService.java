package com.chat.app.service;

import com.chat.app.config.AppProperties;
import com.chat.app.dto.request.ForgotPasswordRequest;
import com.chat.app.dto.request.LoginRequest;
import com.chat.app.dto.request.RefreshTokenRequest;
import com.chat.app.dto.request.RegisterRequest;
import com.chat.app.dto.request.ResetPasswordRequest;
import com.chat.app.dto.response.AuthResponse;
import com.chat.app.dto.response.UserResponse;
import com.chat.app.entity.EmailVerificationToken;
import com.chat.app.entity.PasswordResetToken;
import com.chat.app.entity.RefreshToken;
import com.chat.app.entity.Role;
import com.chat.app.entity.User;
import com.chat.app.enums.RoleName;
import com.chat.app.exception.BadRequestException;
import com.chat.app.exception.UnauthorizedException;
import com.chat.app.mapper.UserMapper;
import com.chat.app.repository.EmailVerificationTokenRepository;
import com.chat.app.repository.PasswordResetTokenRepository;
import com.chat.app.repository.RefreshTokenRepository;
import com.chat.app.repository.RoleRepository;
import com.chat.app.repository.UserRepository;
import com.chat.app.security.CustomUserDetailsService;
import com.chat.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(userRole))
                .build();

        user = userRepository.save(user);
        sendVerificationEmail(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManagerProvider.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userDetailsService.loadUserEntityByEmail(request.getEmail());
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails, user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.isUsed() || verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification token expired or already used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public AuthResponse processOAuth2Login(String email, String googleId, String firstName,
                                           String lastName, String picture) {
        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> createOAuthUser(email, googleId, firstName, lastName, picture));

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
        }
        if (picture != null && user.getProfileImageUrl() == null) {
            user.setProfileImageUrl(picture);
        }
        user.setEmailVerified(true);
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    private User createOAuthUser(String email, String googleId, String firstName,
                                 String lastName, String picture) {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 6);

        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .googleId(googleId)
                .firstName(firstName)
                .lastName(lastName)
                .profileImageUrl(picture)
                .emailVerified(true)
                .roles(Set.of(userRole))
                .build());
    }

    private void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails, user.getId());
        String refreshTokenValue = jwtService.generateRefreshToken(userDetails, user.getId());

        refreshTokenRepository.revokeAllByUserId(user.getId());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(Instant.now().plusMillis(appProperties.getJwt().getRefreshTokenExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }
}
