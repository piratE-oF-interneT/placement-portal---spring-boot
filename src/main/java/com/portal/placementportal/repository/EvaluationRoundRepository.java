package com.portal.placementportal.repository;

import com.portal.placementportal.entity.EvaluationRound;
import com.portal.placementportal.entity.EvaluationRoundId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EvaluationRoundRepository
        extends JpaRepository<EvaluationRound, EvaluationRoundId> {

    Optional<EvaluationRound> findByStudent_StudentIdAndCompany_CompanyId(
            Long studentId, Long companyId);

    /**
     * Bulk fetch tracking rows for a known set of students of a company,
     * eagerly loading the round-status map in the same query. Used by the
     * paginated list endpoint so a page of N students costs one SQL round
     * trip for statuses regardless of N (plus one per page from the
     * {@code @BatchSize} safety net, which never fires here because the
     * JOIN FETCH already populates the collection).
     *
     * {@code select distinct} is required to de-duplicate parent rows
     * that multiply under {@code join fetch} on a collection-valued
     * association.
     */
    @Query("select distinct e from EvaluationRound e "
            + "left join fetch e.rounds "
            + "where e.company.companyId = :companyId "
            + "and e.student.studentId in :studentIds")
    List<EvaluationRound> findWithStatusesByCompanyAndStudents(
            @Param("companyId") Long companyId,
            @Param("studentIds") Collection<Long> studentIds);
}
