package com.template.auth.controller;

import com.template.auth.dto.AuthRequest;
import com.template.auth.dto.AuthResponse;
import com.template.auth.dto.ForgotPasswordRequest;
import com.template.auth.dto.RefreshRequest;
import com.template.auth.dto.ResetPasswordRequest;
import com.template.auth.entity.UserCredentials;
import com.template.auth.repository.UserCredentialsRepository;
import com.template.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserCredentialsRepository userRepository;

    // ─── POST /auth/register ─────────────────────────────────────────────────

    /**
     * Registers a new user account.
     * Returns 201 Created with a confirmation message.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthRequest request) {
        UserCredentials user = UserCredentials.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        authService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully", "username", request.getUsername()));
    }

    // ─── POST /auth/login ────────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a structured response with
     * an accessToken (15min JWT) and a refreshToken (7-day UUID via Redis).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserCredentials user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        return ResponseEntity.ok(authService.generateAuthResponse(user));
    }

    // ─── POST /auth/refresh ──────────────────────────────────────────────────

    /**
     * Issues a new access token using a valid refresh token.
     * The old refresh token is rotated (invalidated and replaced) for security.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshTokens(request.getRefreshToken()));
    }

    // ─── POST /auth/logout ───────────────────────────────────────────────────

    /**
     * Revokes the refresh token from Redis, effectively logging the user out.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ─── POST /auth/forgot-password ──────────────────────────────────────────

    /**
     * Initiates a password reset flow.
     * Always returns 200 OK to prevent email enumeration attacks —
     * the reset token is logged (simulated email) if the email is registered.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "If this email is registered, a reset link has been sent."
        ));
    }

    // ─── POST /auth/reset-password ───────────────────────────────────────────

    /**
     * Resets the user's password using the token received by email.
     * The token is invalidated immediately after use to prevent replay attacks.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. Please log in again."));
    }

    // ─── GET /auth/validate ──────────────────────────────────────────────────

    /**
     * Validates an access token JWT (used internally by the API Gateway).
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken(@RequestParam("token") String token) {
        authService.validateToken(token);
        return ResponseEntity.ok(Map.of("status", "valid"));
    }
}
