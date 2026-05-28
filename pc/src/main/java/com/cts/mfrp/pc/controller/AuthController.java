package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.LoginRequest;
import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import com.cts.mfrp.pc.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials. Please try again.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest regRequest) {
        try {
            User newUser = authService.registerNewUser(regRequest);
            return ResponseEntity.status(201).body(Map.of("message", "Registration successful", "email", newUser.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.processForgotPassword(email);
        } catch (Exception e) {
            // Log the error but don't tell the user the email doesn't exist for security
            System.out.println("Forgot password attempt for non-existent or failed email: " + email);
        }
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            authService.resetPassword(request.get("token"), request.get("newPassword"));
            return ResponseEntity.ok("Password reset successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
