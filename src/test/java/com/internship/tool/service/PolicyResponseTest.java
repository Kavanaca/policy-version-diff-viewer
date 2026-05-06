package com.internship.tool.service;

import com.internship.tool.dto.PolicyResponse;
import com.internship.tool.entity.Policy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyResponseTest {

    @Test
    void fromEntity_Success() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setTitle("Test Policy");
        policy.setContent("Test content here");
        policy.setVersion("1.0");
        policy.setStatus("DRAFT");
        policy.setCreatedBy("admin");
        policy.setDeleted(false);

        PolicyResponse response =
            PolicyResponse.fromEntity(policy);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Policy", response.getTitle());
        assertEquals("Test content here",
            response.getContent());
        assertEquals("1.0", response.getVersion());
        assertEquals("DRAFT", response.getStatus());
        assertEquals("admin", response.getCreatedBy());
    }

    @Test
    void fromEntity_NullFields() {
        Policy policy = new Policy();
        policy.setId(2L);
        policy.setTitle("Minimal Policy");
        policy.setContent("Minimal content here");

        PolicyResponse response =
            PolicyResponse.fromEntity(policy);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Minimal Policy",
            response.getTitle());
    }
}