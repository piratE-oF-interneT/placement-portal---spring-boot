package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByCollege_CollegeId(Long collegeId);
    List<Company> findByCollege_CollegeIdAndActiveTrue(Long collegeId);
}
