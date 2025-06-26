package com.morpheus.security;

import com.morpheus.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        String secret = "my-very-secret-key-for-jwt-signature-which-is-long-enough";
        jwtProperties.setSecret(secret);
        int expiration = 3600;
        jwtProperties.setExpiration(expiration);
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        jwtTokenProvider.init();
    }

    @Test
    void shouldGenerateValidToken() {
        String subject = "testUser";
        String token = jwtTokenProvider.generateToken(subject);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(subject, jwtTokenProvider.getSubject(token));
    }

    @Test
    void shouldInvalidateTamperedToken() {
        String subject = "testUser";
        String token = jwtTokenProvider.generateToken(subject);
        String tamperedToken = token + "tamper";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void shouldParseMultipleRoles() {
        String subject = "testUser";
        var token = jwtTokenProvider.generateToken(subject, java.util.List.of("ADMIN", "USER"));
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(subject, jwtTokenProvider.getSubject(token));
        var roles = jwtTokenProvider.getRoles(token);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
    }
}

