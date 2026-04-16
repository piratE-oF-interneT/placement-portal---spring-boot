package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudent_StudentId(Long studentId);
    List<Registration> findByCompany_CompanyId(Long companyId);
    Optional<Registration> findByStudent_StudentIdAndCompany_CompanyId(Long studentId, Long companyId);
    boolean existsByStudent_StudentIdAndCompany_CompanyId(Long studentId, Long companyId);
}
