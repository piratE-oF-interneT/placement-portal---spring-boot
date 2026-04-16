package com.portal.placementportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class BlacklistDtos {

    public record BlacklistStudentRequest(
            @NotNull(message = "studentId is required")
            @Positive
            Long studentId,

            @NotBlank(message = "reason is required")
            @Size(max = 500)
            String reason
    ) {}
}
