package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.PlacementDtos.PlaceStudentRequest;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Company;
import com.portal.placementportal.entity.Placement;
import com.portal.placementportal.entity.PlacementCategory;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.ForbiddenOperationException;
import com.portal.placementportal.exception.InvalidRequestException;
import com.portal.placementportal.repository.CompanyRepository;
import com.portal.placementportal.repository.PlacementRepository;
import com.portal.placementportal.service.AdminUserService;
import com.portal.placementportal.service.PlacementService;
import com.portal.placementportal.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.portal.placementportal.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class PlacementServiceImpl implements PlacementService {

    private final PlacementRepository placementRepository;
    private final CompanyRepository companyRepository;
    private final AdminUserService adminUserService;
    private final StudentService studentService;

    @Override
    @Transactional
    public Placement placeStudent(Long adminId, PlaceStudentRequest request) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Long adminCollegeId = admin.getCollege().getCollegeId();

        Student student = studentService.getById(request.studentId());
        if (!student.getUsn().equalsIgnoreCase(request.usn())) {
            throw new InvalidRequestException("Provided USN does not match studentId");
        }
        if (!student.getCollege().getCollegeId().equals(adminCollegeId)) {
            throw new CrossCollegeAccessException("Student belongs to a different college");
        }

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.companyId()));
        if (!company.getCollege().getCollegeId().equals(adminCollegeId)) {
            throw new CrossCollegeAccessException("Company belongs to a different college");
        }
        if (company.getCtcLpa() == null) {
            throw new InvalidRequestException("Company CTC must be set before placing a student");
        }

        // A student with an existing NORMAL placement cannot be placed again anywhere.
        if (placementRepository.existsByStudent_StudentIdAndCategory(student.getStudentId(),
                PlacementCategory.NORMAL)) {
            throw new ForbiddenOperationException(
                    "Student is already placed under NORMAL category and cannot be placed again");
        }
        if (placementRepository.existsByStudent_StudentIdAndCompany_CompanyId(
                student.getStudentId(), company.getCompanyId())) {
            throw new DuplicateResourceException("Student is already placed in this company");
        }

        Placement p = Placement.builder()
                .student(student)
                .company(company)
                .category(request.category())
                .ctcLpa(company.getCtcLpa())
                .placedAt(Instant.now())
                .placedBy(admin)
                .build();
        return placementRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Placement> listForStudent(Long studentId) {
        return placementRepository.findByStudent_StudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Placement> listForCompany(Long companyId, Long adminCollegeId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        if (!company.getCollege().getCollegeId().equals(adminCollegeId)) {
            throw new CrossCollegeAccessException("Company belongs to a different college");
        }
        return placementRepository.findByCompany_CompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasNormalPlacement(Long studentId) {
        return placementRepository.existsByStudent_StudentIdAndCategory(studentId, PlacementCategory.NORMAL);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Placement> highestSpecialPlacement(Long studentId) {
        return placementRepository.findByStudent_StudentId(studentId).stream()
                .filter(p -> p.getCategory() == PlacementCategory.SPECIAL)
                .max(Comparator.comparing(Placement::getCtcLpa,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
    }
}
