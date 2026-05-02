package com.internship.tool.service;

import com.internship.tool.dto.PolicyRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyRequestTest {

    @Test
    void policyRequest_SetAndGet() {
        PolicyRequest request = new PolicyRequest();
        request.setTitle("Test Policy");
        request.setContent("Test content here");
        request.setVersion("1.0");
        request.setStatus("DRAFT");
        request.setCreatedBy("admin");

        assertEquals("Test Policy", request.getTitle());
        assertEquals("Test content here",
            request.getContent());
        assertEquals("1.0", request.getVersion());
        assertEquals("DRAFT", request.getStatus());
        assertEquals("admin", request.getCreatedBy());
    }

    @Test
    void policyRequest_DefaultValues() {
        PolicyRequest request = new PolicyRequest();
        assertNull(request.getTitle());
        assertNull(request.getContent());
        assertNull(request.getVersion());
        assertNull(request.getStatus());
    }

    @Test
    void policyRequest_NullTitle() {
        PolicyRequest request = new PolicyRequest();
        request.setTitle(null);
        assertNull(request.getTitle());
    }
}