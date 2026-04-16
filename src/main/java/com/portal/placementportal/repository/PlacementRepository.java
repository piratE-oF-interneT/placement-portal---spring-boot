package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Placement;
import com.portal.placementportal.entity.PlacementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementRepository extends JpaRepository<Placement, Long> {
    List<Placement> findByStudent_StudentId(Long studentId);
    List<Placement> findByCompany_CompanyId(Long companyId);
    boolean existsByStudent_StudentIdAndCategory(Long studentId, PlacementCategory category);
    boolean existsByStudent_StudentIdAndCompany_CompanyId(Long studentId, Long companyId);
}
