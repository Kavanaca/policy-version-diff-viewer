package com.internship.tool.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AiServiceClientTest {

    private AiServiceClient client;

    @BeforeEach
    void setUp() {
        client = new AiServiceClient();
        // Point to a port nothing is running on
        ReflectionTestUtils.setField(client, "aiServiceUrl", "http://localhost:9999");
    }

    @Test
    void describe_returnsNull_whenAiServiceUnreachable() {
        String result = client.describe("Test policy text");
        assertNull(result, "Should return null when AI service is down");
    }

    @Test
    void recommend_returnsNull_whenAiServiceUnreachable() {
        String result = client.recommend("Test policy text");
        assertNull(result, "Should return null when AI service is down");
    }

    void checkHealth_returnsNull_whenAiServiceUnreachable() {
        String result = client.checkHealth();
        assertNull(result, "Should return null when AI service is down");
    }
}