package com.internship.tool.service;

import com.internship.tool.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil,
            "secret",
            "mySecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil,
            "expiration", 86400000L);
    }

    @Test
    void generateToken_NotNull() {
        String token = jwtUtil.generateToken(
            "testuser", "ADMIN");
        assertNotNull(token);
    }

    @Test
    void extractUsername_Success() {
        String token = jwtUtil.generateToken(
            "testuser", "ADMIN");
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void extractRole_Success() {
        String token = jwtUtil.generateToken(
            "testuser", "ADMIN");
        String role = jwtUtil.extractRole(token);
        assertEquals("ADMIN", role);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken(
            "testuser", "ADMIN");
        boolean valid = jwtUtil.validateToken(
            token, "testuser");
        assertTrue(valid);
    }

    @Test
    void validateToken_WrongUsername_ReturnsFalse() {
        String token = jwtUtil.generateToken(
            "testuser", "ADMIN");
        boolean valid = jwtUtil.validateToken(
            token, "wronguser");
        assertFalse(valid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        boolean valid = jwtUtil.validateToken(
            "invalidtoken", "testuser");
        assertFalse(valid);
    }

    @Test
    void extractRole_UserRole() {
        String token = jwtUtil.generateToken(
            "normaluser", "USER");
        String role = jwtUtil.extractRole(token);
        assertEquals("USER", role);
    }

    @Test
    void generateToken_DifferentUsers() {
        String token1 = jwtUtil.generateToken(
            "user1", "ADMIN");
        String token2 = jwtUtil.generateToken(
            "user2", "USER");
        assertNotEquals(token1, token2);
    }
}