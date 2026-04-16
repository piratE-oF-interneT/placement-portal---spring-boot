package com.portal.placementportal.repository;

import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);
    List<AdminUser> findByRole(Role role);
    List<AdminUser> findByCollege_CollegeIdAndRole(Long collegeId, Role role);
}
