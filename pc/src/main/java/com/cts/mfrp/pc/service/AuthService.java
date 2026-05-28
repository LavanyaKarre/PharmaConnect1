package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor // This only injects fields marked as 'final'
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // FIXED: Added 'final' for proper injection

    public User authenticateUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authentication failed: User not found."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials. Please try again.");
        }
        return user;
    }

    public User registerNewUser(RegistrationRequest regRequest) {
        if (userRepository.findByEmail(regRequest.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(regRequest.getName());
        user.setEmail(regRequest.getEmail());
        user.setPhone(regRequest.getPhone());
        user.setRole("BUYER");
        user.setPasswordHash(passwordEncoder.encode(regRequest.getPassword()));

        return userRepository.save(user);
    }

    public void processForgotPassword(String email) {
        // We look for the user. If not found, the Controller handles the generic message.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a unique 36-character token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        // Send the email via Brevo
        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token."));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Token has expired.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Clear token after use
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}