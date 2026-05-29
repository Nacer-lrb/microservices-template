package com.template.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Structured JSON response returned after a successful login or token refresh.
 * Contains both the short-lived accessToken and the long-lived refreshToken.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    /** Token type, always "Bearer" for JWT flows. */
    @Builder.Default
    private String tokenType = "Bearer";

    /** Access token expiration in seconds (e.g. 900 = 15 min). */
    private long expiresIn;

    /** The username of the authenticated user. */
    private String username;

    /** Roles granted to this user for client-side permission checks. */
    private Set<String> roles;
}
