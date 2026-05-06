package com.internship.tool.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            PolicyNotFoundException ex) {
        log.error("Policy not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND,
            ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(
            InvalidInputException ex) {
        log.error("Invalid input: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,
            ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult()
                .getFieldErrors()) {
            errors.put(error.getField(),
                error.getDefaultMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("messages", errors);
        response.put("status", 400);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccess(
            AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN,
            "Access denied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Something went wrong. Please try again.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("status", status.value());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }
}