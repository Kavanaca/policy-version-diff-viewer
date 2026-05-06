package com.internship.tool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    // RestClient is the modern replacement for RestTemplate in Spring Boot 4.x
    private final RestClient restClient;

    public AiServiceClient() {
        this.restClient = RestClient.builder()
                .build();
    }

    // ── 1. POST /describe ──────────────────────────────────────────────────
    public String describe(String policyText) {
        String url = aiServiceUrl + "/describe";
        try {
            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", policyText))
                    .retrieve()
                    .body(String.class);
            logger.info("AI /describe call successful");
            return response;
        } catch (Exception e) {
            logger.error("AI /describe call failed: {}", e.getMessage());
            return null;
        }
    }

    // ── 2. POST /recommend ─────────────────────────────────────────────────
    public String recommend(String policyText) {
        String url = aiServiceUrl + "/recommend";
        try {
            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", policyText))
                    .retrieve()
                    .body(String.class);
            logger.info("AI /recommend call successful");
            return response;
        } catch (Exception e) {
            logger.error("AI /recommend call failed: {}", e.getMessage());
            return null;
        }
    }

    // ── 3. GET /health ─────────────────────────────────────────────────────
    public String checkHealth() {
        String url = aiServiceUrl + "/health";
        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            logger.info("AI service health OK");
            return response;
        } catch (Exception e) {
            logger.error("AI health check failed: {}", e.getMessage());
            return null;
        }
    }
}