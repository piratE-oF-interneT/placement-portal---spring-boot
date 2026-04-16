package com.portal.placementportal.service;

import com.portal.placementportal.entity.Registration;
import com.portal.placementportal.entity.RegistrationStatus;

import java.util.List;

public interface RegistrationService {
    Registration apply(Long studentId, Long companyId);
    List<Registration> listForStudent(Long studentId);
    List<Registration> listForCompany(Long companyId);
    Registration updateStatus(Long registrationId, RegistrationStatus status, Long adminCollegeId);
}
