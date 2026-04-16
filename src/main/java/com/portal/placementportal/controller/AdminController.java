package com.portal.placementportal.controller;

import com.portal.placementportal.dto.AuthDtos.RegistrationStatusUpdateRequest;
import com.portal.placementportal.dto.CompanyDtos.CreateCompanyRequest;
import com.portal.placementportal.dto.CompanyDtos.UpdateCompanyRequest;
import com.portal.placementportal.entity.Company;
import com.portal.placementportal.entity.Registration;
import com.portal.placementportal.entity.RegistrationStatus;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.exception.InvalidRequestException;
import com.portal.placementportal.security.CurrentUser;
import com.portal.placementportal.security.RequestContext;
import com.portal.placementportal.service.CompanyService;
import com.portal.placementportal.service.RegistrationService;
import com.portal.placementportal.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * College-scoped admin dashboard. Every endpoint enforces that the caller is
 * an ADMIN and that the targeted resource belongs to their college.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final CompanyService companyService;
    private final RegistrationService registrationService;
    private final StudentService studentService;
    private final RequestContext requestContext;

    // ---- company management ----

    @PostMapping("/companies")
    public ResponseEntity<Company> createCompany(HttpServletRequest http,
                                                 @Valid @RequestBody CreateCompanyRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(companyService.createForAdmin(cu.userId(), request));
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> listCompanies(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(companyService.listForCollege(cu.collegeId()));
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<Company> getCompany(HttpServletRequest http,
                                              @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(companyService.getScopedToCollege(id, cu.collegeId()));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<Company> updateCompany(HttpServletRequest http,
                                                 @PathVariable @Positive Long id,
                                                 @Valid @RequestBody UpdateCompanyRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(companyService.updateForAdmin(cu.userId(), id, request));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(HttpServletRequest http,
                                              @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        companyService.deleteForAdmin(cu.userId(), id);
        return ResponseEntity.noContent().build();
    }

    // ---- registrations ----

    @GetMapping("/companies/{id}/registrations")
    public ResponseEntity<List<Registration>> listRegistrations(HttpServletRequest http,
                                                                @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        companyService.getScopedToCollege(id, cu.collegeId()); // scope check
        return ResponseEntity.ok(registrationService.listForCompany(id));
    }

    @PutMapping("/registrations/{registrationId}/status")
    public ResponseEntity<Registration> updateRegistrationStatus(
            HttpServletRequest http,
            @PathVariable @Positive Long registrationId,
            @Valid @RequestBody RegistrationStatusUpdateRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        RegistrationStatus status;
        try {
            status = RegistrationStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid registration status: " + request.status());
        }
        return ResponseEntity.ok(registrationService.updateStatus(registrationId, status, cu.collegeId()));
    }

    // ---- students ----

    @GetMapping("/students")
    public ResponseEntity<List<Student>> listStudents(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(studentService.listByCollege(cu.collegeId()));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudent(HttpServletRequest http,
                                              @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        Student s = studentService.getById(id);
        if (!s.getCollege().getCollegeId().equals(cu.collegeId())) {
            throw new CrossCollegeAccessException("Student belongs to a different college");
        }
        return ResponseEntity.ok(s);
    }
}
