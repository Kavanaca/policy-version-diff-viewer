package com.internship.tool.service;

import com.internship.tool.dto.PolicyRequest;
import com.internship.tool.entity.Policy;
import com.internship.tool.exception.InvalidInputException;
import com.internship.tool.exception.PolicyNotFoundException;
import com.internship.tool.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyService policyService;

    private Policy policy;
    private PolicyRequest request;

    @BeforeEach
    void setUp() {
        policy = new Policy();
        policy.setId(1L);
        policy.setTitle("Test Policy");
        policy.setContent("Test content here");
        policy.setVersion("1.0");
        policy.setStatus("DRAFT");
        policy.setDeleted(false);

        request = new PolicyRequest();
        request.setTitle("Test Policy");
        request.setContent("Test content here");
        request.setVersion("1.0");
        request.setStatus("DRAFT");
    }

    @Test
    void createPolicy_Success() {
        when(policyRepository.save(any(Policy.class)))
            .thenReturn(policy);
        Policy result = policyService.createPolicy(request);
        assertNotNull(result);
        assertEquals("Test Policy", result.getTitle());
        verify(policyRepository, times(1))
            .save(any(Policy.class));
    }

    @Test
    void createPolicy_NullTitle_ThrowsException() {
        request.setTitle(null);
        assertThrows(InvalidInputException.class,
            () -> policyService.createPolicy(request));
    }

    @Test
    void createPolicy_EmptyTitle_ThrowsException() {
        request.setTitle("");
        assertThrows(InvalidInputException.class,
            () -> policyService.createPolicy(request));
    }

    @Test
    void createPolicy_NullContent_ThrowsException() {
        request.setContent(null);
        assertThrows(InvalidInputException.class,
            () -> policyService.createPolicy(request));
    }

    @Test
    void createPolicy_ShortContent_ThrowsException() {
        request.setContent("Short");
        assertThrows(InvalidInputException.class,
            () -> policyService.createPolicy(request));
    }

    @Test
    void getPolicyById_Success() {
        when(policyRepository.findByIdAndDeletedFalse(1L))
            .thenReturn(Optional.of(policy));
        Policy result = policyService.getPolicyById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPolicyById_NotFound_ThrowsException() {
        when(policyRepository.findByIdAndDeletedFalse(99L))
            .thenReturn(Optional.empty());
        assertThrows(PolicyNotFoundException.class,
            () -> policyService.getPolicyById(99L));
    }

    @Test
    void deletePolicy_Success() {
        when(policyRepository.findByIdAndDeletedFalse(1L))
            .thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
            .thenReturn(policy);
        policyService.deletePolicy(1L);
        assertTrue(policy.isDeleted());
        verify(policyRepository, times(1))
            .save(any(Policy.class));
    }

    @Test
    void searchByTitle_EmptyKeyword_ThrowsException() {
        assertThrows(InvalidInputException.class,
            () -> policyService.searchByTitle(""));
    }
// ─── Test 11 ───────────────────────────────────────────


    @Test
    void searchByTitle_Success() {
        when(policyRepository
            .findByTitleContainingIgnoreCase("Test"))
            .thenReturn(List.of(policy));
        List<Policy> results =
            policyService.searchByTitle("Test");
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
void updatePolicy_Success() {
    when(policyRepository.findByIdAndDeletedFalse(1L))
        .thenReturn(Optional.of(policy));
    when(policyRepository.save(any(Policy.class)))
        .thenReturn(policy);
    Policy updated =
        policyService.updatePolicy(1L, request);
    assertNotNull(updated);
    verify(policyRepository, times(1))
        .save(any(Policy.class));
}

// ─── Test 12 ───────────────────────────────────────────
@Test
void updatePolicy_NotFound_ThrowsException() {
    when(policyRepository.findByIdAndDeletedFalse(99L))
        .thenReturn(Optional.empty());
    assertThrows(PolicyNotFoundException.class,
        () -> policyService.updatePolicy(99L, request));
}

// ─── Test 13 ───────────────────────────────────────────
@Test
void filterByStatus_Success() {
    when(policyRepository.findByStatus("DRAFT"))
        .thenReturn(List.of(policy));
    List<Policy> results =
        policyService.filterByStatus("DRAFT");
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
}

// ─── Test 14 ───────────────────────────────────────────
@Test
void filterByStatus_Empty_ThrowsException() {
    assertThrows(InvalidInputException.class,
        () -> policyService.filterByStatus(""));
}

// ─── Test 15 ───────────────────────────────────────────
@Test
void createPolicy_NullRequest_ThrowsException() {
    assertThrows(InvalidInputException.class,
        () -> policyService.createPolicy(null));
}
}