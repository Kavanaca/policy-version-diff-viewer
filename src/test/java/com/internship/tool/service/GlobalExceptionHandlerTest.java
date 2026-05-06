package com.internship.tool.service;

import com.internship.tool.exception.GlobalExceptionHandler;
import com.internship.tool.exception.InvalidInputException;
import com.internship.tool.exception.PolicyNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
        new GlobalExceptionHandler();

    @Test
    void handleNotFound_Returns404() {
        PolicyNotFoundException ex =
            new PolicyNotFoundException(1L);
        ResponseEntity<Map<String, Object>> response =
            handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND,
            response.getStatusCode());
        assertEquals(404,
            response.getBody().get("status"));
    }

    @Test
    void handleInvalid_Returns400() {
        InvalidInputException ex =
            new InvalidInputException("Invalid input");
        ResponseEntity<Map<String, Object>> response =
            handler.handleInvalid(ex);
        assertEquals(HttpStatus.BAD_REQUEST,
            response.getStatusCode());
        assertEquals(400,
            response.getBody().get("status"));
    }

    @Test
    void handleAccess_Returns403() {
        AccessDeniedException ex =
            new AccessDeniedException("Access denied");
        ResponseEntity<Map<String, Object>> response =
            handler.handleAccess(ex);
        assertEquals(HttpStatus.FORBIDDEN,
            response.getStatusCode());
        assertEquals(403,
            response.getBody().get("status"));
    }

    @Test
    void handleGeneral_Returns500() {
        Exception ex =
            new Exception("Unexpected error");
        ResponseEntity<Map<String, Object>> response =
            handler.handleGeneral(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
            response.getStatusCode());
        assertEquals(500,
            response.getBody().get("status"));
    }

    @Test
    void handleNotFound_MessageCorrect() {
        PolicyNotFoundException ex =
            new PolicyNotFoundException(5L);
        ResponseEntity<Map<String, Object>> response =
            handler.handleNotFound(ex);
        assertTrue(response.getBody()
            .get("message").toString()
            .contains("5"));
    }

    @Test
    void handleInvalid_MessageCorrect() {
        InvalidInputException ex =
            new InvalidInputException("Title empty");
        ResponseEntity<Map<String, Object>> response =
            handler.handleInvalid(ex);
        assertEquals("Title empty",
            response.getBody().get("message"));
    }
}