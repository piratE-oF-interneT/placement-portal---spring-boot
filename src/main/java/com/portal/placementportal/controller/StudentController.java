package com.portal.placementportal.controller;

import com.portal.placementportal.dto.EntityMapper;
import com.portal.placementportal.dto.ResponseDtos.CompanyResponse;
import com.portal.placementportal.dto.ResponseDtos.RegistrationResponse;
import com.portal.placementportal.dto.ResponseDtos.StudentResponse;
import com.portal.placementportal.dto.StudentDtos.ChangePasswordRequest;
import com.portal.placementportal.dto.StudentDtos.UpdateProfileRequest;
import com.portal.placementportal.entity.Role;
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
 * Student-facing dashboard. All actions are scoped to the caller's own student id
 * and to the companies of their own college. All responses are DTOs; the JPA
 * entities never leave the service layer.
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Validated
public class StudentController {

    private final StudentService studentService;
    private final CompanyService companyService;
    private final RegistrationService registrationService;
    private final RequestContext requestContext;

    @GetMapping("/me")
    public ResponseEntity<StudentResponse> me(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.STUDENT);
        return ResponseEntity.ok(EntityMapper.toStudent(studentService.getById(cu.userId())));
    }

    @PutMapping("/me")
    public ResponseEntity<StudentResponse> updateProfile(HttpServletRequest http,
                                                         @Valid @RequestBody UpdateProfileRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.STUDENT);
        return ResponseEntity.ok(EntityMapper.toStudent(
                studentService.updateProfile(cu.userId(), request)));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(HttpServletRequest http,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.STUDENT);
        studentService.changePassword(cu.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies")
    public ResponseEntity<List<CompanyResponse>> listCompanies(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.STUDENT);
        return ResponseEntity.ok(EntityMapper.mapList(
                companyService.listActiveForCollege(cu.collegeId()), EntityMapper::toCompany));
    }

    @PostMapping("/companies/{companyId}/register")
    public ResponseEntity<RegistrationResponse> registerToCompany(HttpServletRequest http,
                                                                  @PathVariable @Positive Long companyId) {
        CurrentUser cu = requestContext.requireRole(http, Role.STUDENT);
        companyService.getScopedToCollege(companyId, cu.collegeId()); // scope check
        return ResponseEntity.ok(EntityMapper.toRegistration(
                registrationService.apply(cu.userId(), companyId)));
    }

    @GetMapping("/registrations")
    public ResponseEntity<List<RegistrationResponse>> myRegistrations(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.STUDENT);
        return ResponseEntity.ok(EntityMapper.mapList(
                registrationService.listForStudent(cu.userId()), EntityMapper::toRegistration));
    }
}
