package com.portal.placementportal.repository;

import com.portal.placementportal.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    Optional<Credentials> findByLoginIdIgnoreCase(String loginId);
    Optional<Credentials> findByEmailIgnoreCase(String email);
    boolean existsByLoginIdIgnoreCase(String loginId);
    boolean existsByEmailIgnoreCase(String email);
}
