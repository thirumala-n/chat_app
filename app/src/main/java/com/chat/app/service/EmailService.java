package com.chat.app.service;

import com.chat.app.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public void sendVerificationEmail(String to, String token) {
        String link = appProperties.getFrontendUrl() + "/verify-email?token=" + token;
        sendEmail(to, "Verify your email - AI Chat Platform",
                "Click the link to verify your email: " + link);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = appProperties.getFrontendUrl() + "/reset-password?token=" + token;
        sendEmail(to, "Reset your password - AI Chat Platform",
                "Click the link to reset your password: " + link);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
