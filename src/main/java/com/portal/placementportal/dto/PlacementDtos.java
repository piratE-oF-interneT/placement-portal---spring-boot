package com.portal.placementportal.dto;

import com.portal.placementportal.entity.PlacementCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PlacementDtos {

    /**
     * Request payload for an admin placing a student in a company. The caller
     * must supply both studentId and usn — the service verifies they refer to
     * the same student, as a safety check against picking the wrong record.
     */
    public record PlaceStudentRequest(
            @NotNull(message = "companyId is required")
            @Positive(message = "companyId must be positive")
            Long companyId,

            @NotNull(message = "studentId is required")
            @Positive(message = "studentId must be positive")
            Long studentId,

            @NotBlank(message = "usn is required")
            @Size(min = 3, max = 32)
            String usn,

            @NotNull(message = "category is required (NORMAL or SPECIAL)")
            PlacementCategory category
    ) {}
}
