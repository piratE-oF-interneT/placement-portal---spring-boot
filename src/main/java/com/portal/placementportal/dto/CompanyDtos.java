package com.portal.placementportal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CompanyDtos {

    public record CreateCompanyRequest(
            @NotBlank(message = "name is required")
            @Size(max = 255)
            String name,

            @Size(max = 2000)
            String description,

            @Size(max = 255)
            String roleOffered,

            @PositiveOrZero(message = "ctcLpa cannot be negative")
            Double ctcLpa,

            @Size(max = 255)
            String location,

            @DecimalMin(value = "0.0", inclusive = true, message = "minCgpa must be >= 0")
            @DecimalMax(value = "10.0", inclusive = true, message = "minCgpa must be <= 10")
            Double minCgpa,

            @DecimalMin(value = "0.0", inclusive = true, message = "minSscPercentage must be >= 0")
            @DecimalMax(value = "100.0", inclusive = true, message = "minSscPercentage must be <= 100")
            Double minSscPercentage,

            @DecimalMin(value = "0.0", inclusive = true, message = "minHscPercentage must be >= 0")
            @DecimalMax(value = "100.0", inclusive = true, message = "minHscPercentage must be <= 100")
            Double minHscPercentage,

            @PositiveOrZero(message = "maxBacklogs cannot be negative")
            Integer maxBacklogs,

            @Size(max = 255)
            String eligibleBranches,

            LocalDate driveDate,

            LocalDate registrationDeadline
    ) {}

    public record UpdateCompanyRequest(
            @Size(max = 2000) String description,
            @Size(max = 255) String roleOffered,
            @PositiveOrZero Double ctcLpa,
            @Size(max = 255) String location,
            @DecimalMin("0.0") @DecimalMax("10.0") Double minCgpa,
            @DecimalMin("0.0") @DecimalMax("100.0") Double minSscPercentage,
            @DecimalMin("0.0") @DecimalMax("100.0") Double minHscPercentage,
            @PositiveOrZero Integer maxBacklogs,
            @Size(max = 255) String eligibleBranches,
            LocalDate driveDate,
            LocalDate registrationDeadline,
            Boolean active
    ) {}
}
