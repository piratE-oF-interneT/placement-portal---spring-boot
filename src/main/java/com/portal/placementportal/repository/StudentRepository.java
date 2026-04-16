package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUsnIgnoreCase(String usn);
    Optional<Student> findByEmailIgnoreCase(String email);
    boolean existsByUsnIgnoreCase(String usn);
    boolean existsByEmailIgnoreCase(String email);
    List<Student> findByCollege_CollegeId(Long collegeId);
}
