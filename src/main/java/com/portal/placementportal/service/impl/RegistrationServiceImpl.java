package com.portal.placementportal.service.impl;

import com.portal.placementportal.entity.Company;
import com.portal.placementportal.entity.Placement;
import com.portal.placementportal.entity.Registration;
import com.portal.placementportal.entity.RegistrationStatus;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.EligibilityViolationException;
import com.portal.placementportal.exception.ForbiddenOperationException;
import com.portal.placementportal.exception.InvalidRequestException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.CompanyRepository;
import com.portal.placementportal.repository.RegistrationRepository;
import com.portal.placementportal.service.BlacklistService;
import com.portal.placementportal.service.PlacementService;
import com.portal.placementportal.service.RegistrationService;
import com.portal.placementportal.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    /** Dream-offer multiplier — a SPECIAL-placed student may re-apply only to
     *  drives whose CTC is at least this many times the placed CTC. */
    private static final double DREAM_OFFER_MULTIPLIER = 2.0;

    private final RegistrationRepository registrationRepository;
    private final CompanyRepository companyRepository;
    private final StudentService studentService;
    private final PlacementService placementService;
    private final BlacklistService blacklistService;

    @Override
    @Transactional
    public Registration apply(Long studentId, Long companyId) {
        Student student = studentService.getById(studentId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        if (!company.getCollege().getCollegeId().equals(student.getCollege().getCollegeId())) {
            throw new CrossCollegeAccessException("Cannot apply to a company from a different college");
        }
        if (!company.isActive()) {
            throw new InvalidRequestException("Drive is not currently active");
        }
        if (company.getRegistrationDeadline() != null
                && company.getRegistrationDeadline().isBefore(LocalDate.now())) {
            throw new EligibilityViolationException("Registration deadline has passed");
        }
        if (registrationRepository.existsByStudent_StudentIdAndCompany_CompanyId(studentId, companyId)) {
            throw new DuplicateResourceException("Already registered for this company");
        }

        checkBlacklist(student);
        checkPlacementRules(student, company);
        checkAcademicEligibility(student, company);

        Registration r = Registration.builder()
                .student(student)
                .company(company)
                .status(RegistrationStatus.APPLIED)
                .registeredAt(Instant.now())
                .build();
        return registrationRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> listForStudent(Long studentId) {
        return registrationRepository.findByStudent_StudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Registration> listForCompany(Long companyId) {
        return registrationRepository.findByCompany_CompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Registration> listForCompany(
            Long companyId, org.springframework.data.domain.Pageable pageable) {
        return registrationRepository.findByCompany_CompanyId(companyId, pageable);
    }

    @Override
    @Transactional
    public Registration updateStatus(Long registrationId, RegistrationStatus status, Long adminCollegeId) {
        Registration r = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", registrationId));
        if (!r.getCompany().getCollege().getCollegeId().equals(adminCollegeId)) {
            throw new CrossCollegeAccessException("Registration belongs to a different college");
        }
        r.setStatus(status);
        return r;
    }

    @Override
    @Transactional
    public Registration updateStatus(Long registrationId, String rawStatus, Long adminCollegeId) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new InvalidRequestException("status is required");
        }
        RegistrationStatus status;
        try {
            status = RegistrationStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid registration status: " + rawStatus);
        }
        return updateStatus(registrationId, status, adminCollegeId);
    }

    private void checkBlacklist(Student student) {
        if (blacklistService.isBlacklisted(student.getStudentId())) {
            throw new ForbiddenOperationException(
                    "Student is blacklisted and is not eligible to apply to any drive");
        }
    }

    /**
     * Enforces the placement policy:
     *   NORMAL placement → frozen out of all further drives
     *   SPECIAL placement → may only apply to drives whose CTC is at least
     *                       2x the highest SPECIAL-placement CTC
     */
    private void checkPlacementRules(Student student, Company company) {
        if (placementService.hasNormalPlacement(student.getStudentId())) {
            throw new ForbiddenOperationException(
                    "Student is already placed under NORMAL category and cannot apply to further companies");
        }
        Optional<Placement> special = placementService.highestSpecialPlacement(student.getStudentId());
        if (special.isPresent()) {
            Placement placed = special.get();
            if (company.getCtcLpa() == null) {
                throw new EligibilityViolationException(
                        "Company CTC is not set; student with a SPECIAL placement cannot apply");
            }
            double required = placed.getCtcLpa() * DREAM_OFFER_MULTIPLIER;
            if (company.getCtcLpa() < required) {
                throw new EligibilityViolationException(String.format(
                        "Student has a SPECIAL placement at %.2f LPA; eligible only for drives with CTC >= %.2f LPA (got %.2f LPA)",
                        placed.getCtcLpa(), required, company.getCtcLpa()));
            }
        }
    }

    private void checkAcademicEligibility(Student s, Company c) {
        if (c.getMinCgpa() != null && (s.getCgpa() == null || s.getCgpa() < c.getMinCgpa())) {
            throw new EligibilityViolationException("CGPA below required " + c.getMinCgpa());
        }
        if (c.getMinSscPercentage() != null
                && (s.getSscPercentage() == null || s.getSscPercentage() < c.getMinSscPercentage())) {
            throw new EligibilityViolationException(
                    "SSC percentage below required " + c.getMinSscPercentage());
        }
        if (c.getMinHscPercentage() != null
                && (s.getHscPercentage() == null || s.getHscPercentage() < c.getMinHscPercentage())) {
            throw new EligibilityViolationException(
                    "HSC percentage below required " + c.getMinHscPercentage());
        }
        if (c.getMaxBacklogs() != null) {
            int backlogs = s.getCurrentBacklogs() == null ? 0 : s.getCurrentBacklogs();
            if (backlogs > c.getMaxBacklogs()) {
                throw new EligibilityViolationException("Backlogs exceed allowed " + c.getMaxBacklogs());
            }
        }
        if (c.getEligibleBranches() != null && !c.getEligibleBranches().isBlank()) {
            Set<String> allowed = Arrays.stream(c.getEligibleBranches().split(","))
                    .map(String::trim).map(String::toUpperCase)
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toSet());
            String branch = s.getBranch() == null ? "" : s.getBranch().trim().toUpperCase();
            if (!allowed.contains(branch)) {
                throw new EligibilityViolationException("Branch " + s.getBranch() + " not eligible");
            }
        }
    }
}
