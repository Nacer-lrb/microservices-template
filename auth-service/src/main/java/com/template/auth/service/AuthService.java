package com.template.auth.service;

import com.template.auth.dto.AuthResponse;
import com.template.auth.entity.UserCredentials;
import com.template.auth.exception.TokenRefreshException;
import com.template.auth.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String RESET_TOKEN_PREFIX   = "reset:";

    /** Reset token TTL: 15 minutes. */
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);

    private final UserCredentialsRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    /** Refresh token lifespan. Default = 7 days. */
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    // ─── Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a new user by encoding their password and persisting
     * with the default ROLE_USER role.
     */
    public void saveUser(UserCredentials credential) {
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(credential);
        log.info("New user registered: {}", credential.getUsername());
    }

    // ─── Login / Token Issuance ───────────────────────────────────────────────

    /**
     * Generates a full AuthResponse (accessToken + refreshToken) for the given user.
     * Stores the refresh token in Redis with the configured TTL.
     *
     * @param user the authenticated UserCredentials entity from the database
     * @return structured AuthResponse containing both tokens and metadata
     */
    public AuthResponse generateAuthResponse(UserCredentials user) {
        // Build a short-lived signed JWT embedding the user's roles
        String accessToken = jwtService.generateToken(user.getUsername(), user.getRoles());

        // Build a long-lived opaque refresh token and store it in Redis
        String refreshToken = UUID.randomUUID().toString();
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(
                redisKey,
                user.getUsername(),
                Duration.ofMillis(refreshExpiration)
        );
        log.debug("Refresh token stored in Redis for user: {}", user.getUsername());

        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationSeconds())
                .username(user.getUsername())
                .roles(roles)
                .build();
    }

    // ─── Token Refresh ────────────────────────────────────────────────────────

    /**
     * Validates the given refresh token against Redis, then issues a new
     * access token (and rotates the refresh token for maximum security).
     *
     * @param refreshToken the opaque refresh token UUID from the client
     * @return a fresh AuthResponse with new tokens
     */
    public AuthResponse refreshTokens(String refreshToken) {
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String username = redisTemplate.opsForValue().get(redisKey);

        if (username == null) {
            throw new TokenRefreshException(refreshToken, "Token not found or has expired");
        }

        UserCredentials user = repository.findByUsername(username)
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "User no longer exists"));

        // Invalidate the old refresh token (rotation prevents replay attacks)
        redisTemplate.delete(redisKey);

        return generateAuthResponse(user);
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    /**
     * Invalidates the given refresh token from Redis, effectively logging the user out
     * on all devices that relied on this token.
     *
     * @param refreshToken the opaque refresh token UUID to revoke
     */
    public void logout(String refreshToken) {
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Boolean deleted = redisTemplate.delete(redisKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Refresh token revoked successfully.");
        } else {
            log.warn("Logout attempted with an unknown or already-expired refresh token.");
        }
    }

    // ─── Token Validation (used by Gateway) ──────────────────────────────────

    /**
     * Validates the JWT signature and expiry. Throws JwtException on failure.
     */
    public void validateToken(String token) {
        jwtService.validateToken(token);
    }

    // ─── Forgot / Reset Password ──────────────────────────────────────────────

    /**
     * Initiates a password reset flow for the given email.
     *
     * <p>A secure reset token is generated and stored in Redis for 15 minutes.
     * In production, this token would be sent via email (e.g. via notification-service).
     * Here it is logged so you can test the flow directly.
     *
     * @param email the registered email address of the user
     */
    public void forgotPassword(String email) {
        // Look up user — but NEVER reveal whether the email exists for security
        repository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            String redisKey   = RESET_TOKEN_PREFIX + resetToken;

            // Store resetToken → email in Redis (15-minute TTL)
            redisTemplate.opsForValue().set(redisKey, email, RESET_TOKEN_TTL);

            // ──────────────────────────────────────────────────────────────────
            // TODO: Replace this log with a real email dispatch via notification-service
            // e.g. publish a Kafka event: { type: "PASSWORD_RESET", email, token }
            // ──────────────────────────────────────────────────────────────────
            log.info("[PASSWORD RESET] Token for {} : {}", email, resetToken);
            log.info("[SIMULATED EMAIL] → To: {}  Subject: Reset your password", email);
            log.info("[SIMULATED EMAIL] Use this token within 15 minutes: {}", resetToken);
        });

        // Always return success to prevent email enumeration attacks
        log.debug("Password reset flow completed for email: {}", email);
    }

    /**
     * Resets the user's password using a valid reset token.
     *
     * <p>The token is validated against Redis, the password is updated,
     * and the token is immediately invalidated to prevent reuse.
     *
     * @param resetToken  the opaque reset token from the email
     * @param newPassword the new plain-text password (will be encoded)
     */
    public void resetPassword(String resetToken, String newPassword) {
        String redisKey = RESET_TOKEN_PREFIX + resetToken;
        String email    = redisTemplate.opsForValue().get(redisKey);

        if (email == null) {
            throw new TokenRefreshException(resetToken, "Reset token is invalid or has expired");
        }

        UserCredentials user = repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + email));

        // Update and persist the new encoded password
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);

        // Immediately invalidate the reset token so it cannot be reused
        redisTemplate.delete(redisKey);

        log.info("Password successfully reset for user: {}", user.getUsername());
    }
}
