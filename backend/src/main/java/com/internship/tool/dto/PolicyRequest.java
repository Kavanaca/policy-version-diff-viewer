package com.internship.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PolicyRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Content cannot be empty")
    @Size(min = 10,
        message = "Content must be at least 10 characters")
    private String content;

    private String version;
    private String status;
    private String createdBy;
}