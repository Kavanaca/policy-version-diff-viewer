package com.internship.tool.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PolicyResponse {

    private Long id;
    private String title;
    private String content;
    private String version;
    private String status;
    private String aiSummary;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PolicyResponse fromEntity(
        com.internship.tool.entity.Policy policy) {

        PolicyResponse response = new PolicyResponse();
        response.setId(policy.getId());
        response.setTitle(policy.getTitle());
        response.setContent(policy.getContent());
        response.setVersion(policy.getVersion());
        response.setStatus(policy.getStatus());
        response.setAiSummary(policy.getAiSummary());
        response.setCreatedBy(policy.getCreatedBy());
        response.setCreatedAt(policy.getCreatedAt());
        response.setUpdatedAt(policy.getUpdatedAt());
        return response;
    }
}