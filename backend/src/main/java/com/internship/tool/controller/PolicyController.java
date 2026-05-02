package com.internship.tool.controller;

import com.internship.tool.dto.PolicyRequest;
import com.internship.tool.dto.PolicyResponse;
import com.internship.tool.entity.Policy;
import com.internship.tool.service.EmailService;
import com.internship.tool.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final EmailService emailService;

    // ─── GET ALL ───────────────────────────────────────────
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<PolicyResponse>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/policies/all");
        Page<PolicyResponse> policies = policyService
                .getAllPolicies(PageRequest.of(page, size,
                    Sort.by("createdAt").descending()))
                .map(PolicyResponse::fromEntity);
        return ResponseEntity.ok(policies);
    }

    // ─── GET BY ID ─────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<PolicyResponse> getPolicyById(
            @PathVariable Long id) {
        log.info("GET /api/policies/{}", id);
        Policy policy = policyService.getPolicyById(id);
        return ResponseEntity.ok(PolicyResponse.fromEntity(policy));
    }

    // ─── CREATE ────────────────────────────────────────────
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PolicyResponse> createPolicy(
            @Valid @RequestBody PolicyRequest request) {
        log.info("POST /api/policies/create");
        Policy created = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PolicyResponse.fromEntity(created));
    }

    // ─── UPDATE ────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyRequest request) {
        log.info("PUT /api/policies/{}", id);
        Policy updated = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(PolicyResponse.fromEntity(updated));
    }

    // ─── DELETE ────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deletePolicy(
            @PathVariable Long id) {
        log.info("DELETE /api/policies/{}", id);
        policyService.deletePolicy(id);
        return ResponseEntity.ok("Policy deleted successfully");
    }

    // ─── SEARCH ────────────────────────────────────────────
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<PolicyResponse>> searchPolicies(
            @RequestParam String q) {
        log.info("GET /api/policies/search?q={}", q);
        List<PolicyResponse> results = policyService
                .searchByTitle(q)
                .stream()
                .map(PolicyResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // ─── FILTER ────────────────────────────────────────────
    @GetMapping("/filter")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<PolicyResponse>> filterByStatus(
            @RequestParam String status) {
        log.info("GET /api/policies/filter?status={}", status);
        List<PolicyResponse> results = policyService
                .filterByStatus(status)
                .stream()
                .map(PolicyResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // ─── TEST EMAIL — Remove before Demo Day ──────────────
    @GetMapping("/test-email")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
public ResponseEntity<String> testEmail() {
        log.info("GET /api/policies/test-email");
        emailService.sendReminderEmail(
            "kavanaca2005@gmail.com",
            "Admin",
            "Test Policy",
            "DRAFT",
            "1.0"
        );
        return ResponseEntity.ok("Email triggered — check inbox");
    }
}