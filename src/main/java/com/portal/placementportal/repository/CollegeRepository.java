package com.portal.placementportal.repository;

import com.portal.placementportal.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {
    Optional<College> findByCollegeNameIgnoreCase(String collegeName);
    boolean existsByCollegeNameIgnoreCase(String collegeName);
}
