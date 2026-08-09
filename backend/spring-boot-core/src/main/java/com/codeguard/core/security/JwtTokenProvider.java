package com.codeguard.core.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long jwtExpirationInMs;
    private final long refreshExpirationInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:super_secret_jwt_token_for_codeguard_ai_dev_environment_32_bytes_long}") String secret,
            @Value("${jwt.expiration:3600000}") long jwtExpirationInMs,
            @Value("${jwt.refresh-expiration:604800000}") long refreshExpirationInMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.refreshExpirationInMs = refreshExpirationInMs;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), jwtExpirationInMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), refreshExpirationInMs);
    }

    public String generateToken(String username) {
        return generateToken(username, jwtExpirationInMs);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshExpirationInMs);
    }

    private String generateToken(String username, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log details here if needed
        }
        return false;
    }
}
