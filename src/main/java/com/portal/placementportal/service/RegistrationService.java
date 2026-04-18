package com.portal.placementportal.service;

import com.portal.placementportal.entity.Registration;
import com.portal.placementportal.entity.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RegistrationService {
    Registration apply(Long studentId, Long companyId);
    List<Registration> listForStudent(Long studentId);
    List<Registration> listForCompany(Long companyId);
    Page<Registration> listForCompany(Long companyId, Pageable pageable);
    Registration updateStatus(Long registrationId, RegistrationStatus status, Long adminCollegeId);

    /**
     * String-accepting variant: controllers pass the raw status from the
     * request body; parsing and validation happen here so error shapes are
     * consistent with other invalid-input failures.
     */
    Registration updateStatus(Long registrationId, String rawStatus, Long adminCollegeId);
}
