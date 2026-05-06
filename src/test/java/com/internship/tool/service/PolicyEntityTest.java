package com.internship.tool.service;

import com.internship.tool.entity.Policy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyEntityTest {

    @Test
    void policy_SetAndGet() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setTitle("Test Policy");
        policy.setContent("Test content here");
        policy.setVersion("1.0");
        policy.setStatus("DRAFT");
        policy.setCreatedBy("admin");
        policy.setDeleted(false);

        assertEquals(1L, policy.getId());
        assertEquals("Test Policy", policy.getTitle());
        assertEquals("Test content here",
            policy.getContent());
        assertEquals("1.0", policy.getVersion());
        assertEquals("DRAFT", policy.getStatus());
        assertEquals("admin", policy.getCreatedBy());
        assertFalse(policy.isDeleted());
    }

    @Test
    void policy_DefaultValues() {
        Policy policy = new Policy();
        assertNull(policy.getId());
        assertNull(policy.getTitle());
        assertFalse(policy.isDeleted());
    }

    @Test
    void policy_SetDeleted() {
        Policy policy = new Policy();
        policy.setDeleted(true);
        assertTrue(policy.isDeleted());
    }

    @Test
    void policy_SetAiSummary() {
        Policy policy = new Policy();
        policy.setAiSummary("This is AI summary");
        assertEquals("This is AI summary",
            policy.getAiSummary());
    }
}