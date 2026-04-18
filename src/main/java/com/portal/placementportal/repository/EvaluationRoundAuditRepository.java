package com.portal.placementportal.repository;

import com.portal.placementportal.entity.EvaluationRoundAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EvaluationRoundAuditRepository
        extends JpaRepository<EvaluationRoundAudit, Long> {

    /** History for a single (student, company) pair, oldest first. */
    List<EvaluationRoundAudit>
    findByStudent_StudentIdAndCompany_CompanyIdOrderByChangedAtAsc(
            Long studentId, Long companyId);

    /**
     * Batch fetch history for a specific set of students of a company, used
     * by the paginated list endpoint so audit queries are scoped to the
     * page rather than the entire company. Sorted by {@code changed_at}
     * so the service can group-by-student without a second pass.
     */
    List<EvaluationRoundAudit>
    findByCompany_CompanyIdAndStudent_StudentIdInOrderByChangedAtAsc(
            Long companyId, Collection<Long> studentIds);
}
