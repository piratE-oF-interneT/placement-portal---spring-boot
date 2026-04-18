package com.portal.placementportal.dto;

import com.portal.placementportal.entity.PlacementCategory;
import com.portal.placementportal.entity.RegistrationStatus;
import com.portal.placementportal.entity.Role;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response-side DTOs. One record per entity the API surface exposes. Kept
 * in a single file because they are purely transport shapes — the
 * mappings that produce them live in {@link EntityMapper}.
 *
 * Rationale: we never serialize JPA entities directly out of controllers.
 * That leaks persistence concerns (lazy proxies, bidirectional refs,
 * optimistic-lock version fields) into the public API and tightly couples
 * external consumers to the schema.
 */
public class ResponseDtos {

    public record CompanyResponse(
            Long companyId,
            String name,
            String description,
            String roleOffered,
            Double ctcLpa,
            String location,
            Double minCgpa,
            Double minSscPercentage,
            Double minHscPercentage,
            Integer maxBacklogs,
            String eligibleBranches,
            LocalDate driveDate,
            LocalDate registrationDeadline,
            boolean active,
            Long collegeId,
            Long createdByAdminId
    ) {}

    public record RegistrationResponse(
            Long registrationId,
            Long studentId,
            String studentUsn,
            String studentFullName,
            Long companyId,
            String companyName,
            RegistrationStatus status,
            Instant registeredAt,
            String notes
    ) {}

    public record StudentResponse(
            Long studentId,
            String usn,
            String email,
            String fullName,
            String phone,
            LocalDate dateOfBirth,
            String gender,
            String address,
            String branch,
            Integer batchYear,
            Double sscPercentage,
            Double hscPercentage,
            Double cgpa,
            Double sem1Gpa,
            Double sem2Gpa,
            Double sem3Gpa,
            Double sem4Gpa,
            Double sem5Gpa,
            Double sem6Gpa,
            Double sem7Gpa,
            Double sem8Gpa,
            Integer currentBacklogs,
            boolean profileComplete,
            Long collegeId
    ) {}

    public record PlacementResponse(
            Long placementId,
            Long studentId,
            String studentUsn,
            Long companyId,
            String companyName,
            PlacementCategory category,
            Double ctcLpa,
            Instant placedAt,
            Long placedByAdminId
    ) {}

    public record BlacklistResponse(
            Long blacklistId,
            Long studentId,
            String studentUsn,
            String reason,
            Instant blacklistedAt,
            Long blacklistedByAdminId
    ) {}

    public record CollegeResponse(
            Long collegeId,
            String collegeName,
            String address
    ) {}

    public record AdminUserResponse(
            Long adminId,
            String username,
            String fullName,
            String email,
            Role role,
            Long collegeId
    ) {}
}
