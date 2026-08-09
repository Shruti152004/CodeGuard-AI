package com.codeguard.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTests {

    private JwtTokenProvider tokenProvider;
    private final String secret = "super_secret_jwt_token_for_codeguard_ai_dev_environment_32_bytes_long";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, 3600000, 604800000);
    }

    @Test
    void testGenerateAndValidateToken() {
        // Generate token
        String username = "testuser";
        String token = tokenProvider.generateToken(username);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));

        // Get username
        String parsedUsername = tokenProvider.getUsernameFromJWT(token);
        assertEquals(username, parsedUsername);
    }

    @Test
    void testInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalidToken"));
    }
}
