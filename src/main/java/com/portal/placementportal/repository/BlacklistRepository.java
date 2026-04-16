package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
    Optional<Blacklist> findByStudent_StudentId(Long studentId);
    boolean existsByStudent_StudentId(Long studentId);
    List<Blacklist> findByStudent_College_CollegeId(Long collegeId);
}
