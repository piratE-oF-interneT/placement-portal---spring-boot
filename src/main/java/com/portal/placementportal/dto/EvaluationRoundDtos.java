package com.portal.placementportal.dto;

import com.portal.placementportal.entity.EvaluationRoundType;
import com.portal.placementportal.entity.RoundStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public class EvaluationRoundDtos {

    /**
     * Request body for both add-round and remove-round endpoints. The round
     * field is a free-form String here; parsing lives in
     * {@link com.portal.placementportal.service.EvaluationRoundService} so
     * invalid values yield a consistent {@code InvalidRequestException}
     * rather than a Jackson deserialisation error.
     */
    public record RoundUpdateRequest(
            @NotBlank(message = "round is required")
            String round
    ) {}

    /** Current state of a single round. */
    public record RoundStateDto(
            EvaluationRoundType round,
            RoundStatus status,
            Instant updatedAt
    ) {}

    /** Single audit entry in the history timeline. */
    public record AuditEntryDto(
            Long auditId,
            EvaluationRoundType round,
            RoundStatus oldStatus,
            RoundStatus newStatus,
            Instant changedAt,
            Long changedByAdminId,
            String changedByUsername
    ) {}

    /**
     * Progress for a single (student, company) pair — returned by the
     * update endpoints and embedded inside the list response's page.
     */
    public record StudentRoundProgress(
            Long studentId,
            String usn,
            String fullName,
            String email,
            Long companyId,
            String companyName,
            List<RoundStateDto> rounds,
            List<AuditEntryDto> history
    ) {}

    /**
     * List endpoint response: paginated progress for a company's registered
     * students. Pagination metadata is inlined here (rather than wrapping
     * the whole thing in a PageResponse) so the company-level fields stay
     * at the top of the JSON payload and clients don't have to reach into
     * content[0] to get the company name.
     */
    public record CompanyRoundProgressResponse(
            Long companyId,
            String companyName,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last,
            List<StudentRoundProgress> students
    ) {}
}
