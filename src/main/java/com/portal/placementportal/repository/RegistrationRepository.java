package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudent_StudentId(Long studentId);

    List<Registration> findByCompany_CompanyId(Long companyId);

    Optional<Registration> findByStudent_StudentIdAndCompany_CompanyId(Long studentId, Long companyId);

    boolean existsByStudent_StudentIdAndCompany_CompanyId(Long studentId, Long companyId);

    /**
     * Paginated registrations for a company. Used by the evaluation-rounds
     * list endpoint to avoid loading every registered student at once on
     * large drives. The entity graph pre-loads student + company so the
     * mapping pass does not trigger N+1 association fetches.
     */
    @EntityGraph(attributePaths = { "student", "company" })
    Page<Registration> findByCompany_CompanyId(Long companyId, Pageable pageable);
}
