package com.portal.placementportal.service;

import com.portal.placementportal.dto.EvaluationRoundDtos.CompanyRoundProgressResponse;
import com.portal.placementportal.dto.EvaluationRoundDtos.StudentRoundProgress;
import org.springframework.data.domain.Pageable;

public interface EvaluationRoundService {

    /**
     * Paginated list: every registered student of the given company (scoped
     * to the caller's college), together with their per-round progress and
     * audit history for the slice described by {@code pageable}. Audit and
     * status fetches are batched over the page contents rather than the
     * full company roster.
     */
    CompanyRoundProgressResponse listForCompany(Long companyId, Long adminCollegeId,
                                                Pageable pageable);

    /**
     * Mark a round as CLEARED for the (student, company) pair. Every earlier
     * round in the pipeline is also marked CLEARED. Round parsing and
     * conflict-retry live in the service so controllers stay thin.
     */
    StudentRoundProgress addRound(Long studentId, Long companyId,
                                  String round, Long adminId);

    /**
     * Mark a round as FAILED for the (student, company) pair. Every later
     * round in the pipeline is reset to PENDING.
     */
    StudentRoundProgress removeRound(Long studentId, Long companyId,
                                     String round, Long adminId);
}
