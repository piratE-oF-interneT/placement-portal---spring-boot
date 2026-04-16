package com.portal.placementportal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class StudentDtos {

    public record UpdateProfileRequest(
            @Size(max = 255) String fullName,
            @Size(max = 20) String phone,
            LocalDate dateOfBirth,
            @Size(max = 16) String gender,
            @Size(max = 500) String address,
            @Size(max = 64) String branch,
            @PositiveOrZero Integer batchYear,
            @DecimalMin("0.0") @DecimalMax("100.0") Double sscPercentage,
            @DecimalMin("0.0") @DecimalMax("100.0") Double hscPercentage,
            @DecimalMin("0.0") @DecimalMax("10.0") Double cgpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem1Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem2Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem3Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem4Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem5Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem6Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem7Gpa,
            @DecimalMin("0.0") @DecimalMax("10.0") Double sem8Gpa,
            @PositiveOrZero Integer currentBacklogs
    ) {}

    public record ChangePasswordRequest(
            @NotBlank(message = "oldPassword is required") String oldPassword,
            @NotBlank(message = "newPassword is required")
            @Size(min = 8, message = "newPassword must be at least 8 characters") String newPassword
    ) {}
}
