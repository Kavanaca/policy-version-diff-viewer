package com.internship.tool.service;

import com.internship.tool.entity.Policy;
import com.internship.tool.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PolicyRepositoryTest {

    @Autowired
    private PolicyRepository policyRepository;

    private Policy testPolicy;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();
        testPolicy = new Policy();
        testPolicy.setTitle("IT Security Policy");
        testPolicy.setContent(
            "This is IT security content here");
        testPolicy.setVersion("1.0");
        testPolicy.setStatus("ACTIVE");
        testPolicy.setDeleted(false);
        policyRepository.save(testPolicy);
    }

    @Test
    void findByIdAndDeletedFalse_Success() {
        Optional<Policy> result =
            policyRepository.findByIdAndDeletedFalse(
                testPolicy.getId());
        assertTrue(result.isPresent());
    }

    @Test
    void findByIdAndDeletedFalse_Deleted_Empty() {
        testPolicy.setDeleted(true);
        policyRepository.save(testPolicy);
        Optional<Policy> result =
            policyRepository.findByIdAndDeletedFalse(
                testPolicy.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void findByTitle_Success() {
        List<Policy> results = policyRepository
            .findByTitleContainingIgnoreCase("security");
        assertFalse(results.isEmpty());
    }

    @Test
    void findByTitle_CaseInsensitive() {
        List<Policy> results = policyRepository
            .findByTitleContainingIgnoreCase("SECURITY");
        assertFalse(results.isEmpty());
    }

    @Test
    void findByTitle_NotFound() {
        List<Policy> results = policyRepository
            .findByTitleContainingIgnoreCase("xyz123");
        assertTrue(results.isEmpty());
    }

    @Test
    void findByStatus_Active() {
        List<Policy> results =
            policyRepository.findByStatus("ACTIVE");
        assertFalse(results.isEmpty());
    }

    @Test
    void findByStatus_NotFound() {
        List<Policy> results =
            policyRepository.findByStatus("DRAFT");
        assertTrue(results.isEmpty());
    }

    @Test
    void save_Success() {
        Policy policy = new Policy();
        policy.setTitle("New Policy");
        policy.setContent("New content here test");
        policy.setVersion("1.0");
        policy.setStatus("DRAFT");
        policy.setDeleted(false);
        Policy saved = policyRepository.save(policy);
        assertNotNull(saved.getId());
    }

    @Test
    void softDelete_Works() {
        testPolicy.setDeleted(true);
        policyRepository.save(testPolicy);
        Optional<Policy> result =
            policyRepository.findByIdAndDeletedFalse(
                testPolicy.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void count_Correct() {
        long count = policyRepository.count();
        assertEquals(1, count);
    }
}