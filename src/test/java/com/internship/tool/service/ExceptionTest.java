package com.internship.tool.service;

import com.internship.tool.exception.InvalidInputException;
import com.internship.tool.exception.PolicyNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void policyNotFoundException_Message() {
        PolicyNotFoundException ex =
            new PolicyNotFoundException(1L);
        assertEquals("Policy not found with id: 1",
            ex.getMessage());
    }

    @Test
    void policyNotFoundException_DifferentId() {
        PolicyNotFoundException ex =
            new PolicyNotFoundException(99L);
        assertEquals("Policy not found with id: 99",
            ex.getMessage());
    }

    @Test
    void invalidInputException_Message() {
        InvalidInputException ex =
            new InvalidInputException("Title is empty");
        assertEquals("Title is empty", ex.getMessage());
    }

    @Test
    void invalidInputException_NullMessage() {
        InvalidInputException ex =
            new InvalidInputException("Content is empty");
        assertEquals("Content is empty", ex.getMessage());
    }
}