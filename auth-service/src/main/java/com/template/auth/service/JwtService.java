package com.template.auth.service;

import com.template.auth.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Access token lifespan in ms. Default = 15 minutes (900_000 ms).
     * Keep short for security — the refresh token handles session renewal.
     */
    @Value("${jwt.expiration:900000}")
    private long expiration;

    // ─── Token Generation ─────────────────────────────────────────────────────

    /**
     * Generates a signed JWT embedding the user's username and roles as claims.
     *
     * @param username the authenticated user's username
     * @param roles    the set of roles to embed in the token
     * @return signed JWT string
     */
    public String generateToken(String username, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        // Embed roles as a list of strings so the Gateway can parse them
        claims.put("roles", roles.stream().map(Enum::name).collect(Collectors.toList()));
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    // ─── Token Validation & Parsing ──────────────────────────────────────────

    /**
     * Validates the JWT signature and expiry. Throws JwtException on failure.
     *
     * @param token the raw JWT string
     */
    public void validateToken(final String token) {
        Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
    }

    /**
     * Extracts the username (subject) from a valid JWT.
     *
     * @param token the raw JWT string
     * @return the username embedded as the token subject
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Returns the configured access token expiration in seconds (for AuthResponse).
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }

    // ─── Key ─────────────────────────────────────────────────────────────────

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
