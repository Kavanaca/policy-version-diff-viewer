package com.internship.tool.service;

import com.internship.tool.dto.PolicyRequest;
import com.internship.tool.entity.Policy;
import com.internship.tool.exception.InvalidInputException;
import com.internship.tool.exception.PolicyNotFoundException;
import com.internship.tool.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {

    private final PolicyRepository policyRepository;

    // ─── GET ALL — Cached ──────────────────────────────────
    @Cacheable(value = "policies-list")
    public Page<Policy> getAllPolicies(Pageable pageable) {
        log.info("Fetching all policies — from DB");
        return policyRepository.findAll(pageable);
    }

    // ─── GET BY ID — Cached ────────────────────────────────
    @Cacheable(value = "policies", key = "#id")
    public Policy getPolicyById(Long id) {
        log.info("Fetching policy id: {} — from DB", id);
        return policyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                    new PolicyNotFoundException(id));
    }

    // ─── CREATE — Evict Cache ──────────────────────────────
    @CacheEvict(value = {"policies", "policies-list",
            "policy-search"}, allEntries = true)
    public Policy createPolicy(PolicyRequest request) {
        // validate FIRST before accessing request fields
        validateRequest(request);
        log.info("Creating policy: {}", request.getTitle());
        Policy policy = new Policy();
        policy.setTitle(request.getTitle());
        policy.setContent(request.getContent());
        policy.setVersion(request.getVersion() != null
            ? request.getVersion() : "1.0");
        policy.setStatus(request.getStatus() != null
            ? request.getStatus() : "DRAFT");
        policy.setCreatedBy(request.getCreatedBy());
        Policy saved = policyRepository.save(policy);
        log.info("Policy created id: {}", saved.getId());
        return saved;
    }

    // ─── UPDATE — Evict Cache ──────────────────────────────
    @CacheEvict(value = {"policies", "policies-list",
            "policy-search"}, allEntries = true)
    public Policy updatePolicy(Long id,
            PolicyRequest request) {
        log.info("Updating policy id: {}", id);
        // validate FIRST before accessing request fields
        validateRequest(request);
        Policy policy = getPolicyById(id);
        policy.setTitle(request.getTitle());
        policy.setContent(request.getContent());
        if (request.getVersion() != null) {
            policy.setVersion(request.getVersion());
        }
        if (request.getStatus() != null) {
            policy.setStatus(request.getStatus());
        }
        Policy updated = policyRepository.save(policy);
        log.info("Policy updated id: {}", updated.getId());
        return updated;
    }

    // ─── DELETE — Evict Cache ──────────────────────────────
    @CacheEvict(value = {"policies", "policies-list",
            "policy-search"}, allEntries = true)
    public void deletePolicy(Long id) {
        log.info("Deleting policy id: {}", id);
        Policy policy = getPolicyById(id);
        policy.setDeleted(true);
        policyRepository.save(policy);
        log.info("Policy deleted id: {}", id);
    }

    // ─── SEARCH — Cached ───────────────────────────────────
    @Cacheable(value = "policy-search", key = "#title")
    public List<Policy> searchByTitle(String title) {
        log.info("Searching: {}", title);
        if (title == null || title.isBlank()) {
            throw new InvalidInputException(
                "Search keyword cannot be empty");
        }
        return policyRepository
            .findByTitleContainingIgnoreCase(title);
    }

    // ─── FILTER BY STATUS ──────────────────────────────────
    public List<Policy> filterByStatus(String status) {
        log.info("Filter by status: {}", status);
        if (status == null || status.isBlank()) {
            throw new InvalidInputException(
                "Status cannot be empty");
        }
        return policyRepository.findByStatus(status);
    }


    // ─── VALIDATION ────────────────────────────────────────
    private void validateRequest(PolicyRequest request) {
        if (request == null) {
            throw new InvalidInputException(
                "Request cannot be null");
        }
        if (request.getTitle() == null
                || request.getTitle().isBlank()) {
            throw new InvalidInputException(
                "Title cannot be empty");
        }
        if (request.getContent() == null
                || request.getContent().isBlank()) {
            throw new InvalidInputException(
                "Content cannot be empty");
        }
        if (request.getContent().length() < 10) {
            throw new InvalidInputException(
                "Content must be at least 10 characters");
        }
    }
    // ─── HEALTH CHECK ──────────────────────────────────────
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", "Policy Service"
        );
    }
}
}