package com.portal.placementportal.controller;

import com.portal.placementportal.dto.AuthDtos.RegistrationStatusUpdateRequest;
import com.portal.placementportal.dto.CompanyDtos.CreateCompanyRequest;
import com.portal.placementportal.dto.CompanyDtos.UpdateCompanyRequest;
import com.portal.placementportal.dto.EntityMapper;
import com.portal.placementportal.dto.EvaluationRoundDtos.CompanyRoundProgressResponse;
import com.portal.placementportal.dto.EvaluationRoundDtos.RoundUpdateRequest;
import com.portal.placementportal.dto.EvaluationRoundDtos.StudentRoundProgress;
import com.portal.placementportal.dto.PageResponse;
import com.portal.placementportal.dto.ResponseDtos.CompanyResponse;
import com.portal.placementportal.dto.ResponseDtos.RegistrationResponse;
import com.portal.placementportal.dto.ResponseDtos.StudentResponse;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.security.CurrentUser;
import com.portal.placementportal.security.RequestContext;
import com.portal.placementportal.service.CompanyService;
import com.portal.placementportal.service.EvaluationRoundService;
import com.portal.placementportal.service.RegistrationService;
import com.portal.placementportal.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * College-scoped admin dashboard. Every endpoint enforces that the caller is
 * an ADMIN and that the targeted resource belongs to their college.
 *
 * All responses are DTOs built via {@link EntityMapper}; JPA entities are
 * never serialised directly, which keeps lazy proxies, version columns,
 * and bidirectional references out of the public API surface.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    /** Upper bound on page size to keep the DB from being asked for enormous pages. */
    private static final int MAX_PAGE_SIZE = 200;

    private final CompanyService companyService;
    private final RegistrationService registrationService;
    private final StudentService studentService;
    private final EvaluationRoundService evaluationRoundService;
    private final RequestContext requestContext;

    // ---- company management ----

    @PostMapping("/companies")
    public ResponseEntity<CompanyResponse> createCompany(HttpServletRequest http,
                                                         @Valid @RequestBody CreateCompanyRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.toCompany(
                companyService.createForAdmin(cu.userId(), request)));
    }

    @GetMapping("/companies")
    public ResponseEntity<List<CompanyResponse>> listCompanies(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.mapList(
                companyService.listForCollege(cu.collegeId()), EntityMapper::toCompany));
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> getCompany(HttpServletRequest http,
                                                      @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.toCompany(
                companyService.getScopedToCollege(id, cu.collegeId())));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(HttpServletRequest http,
                                                         @PathVariable @Positive Long id,
                                                         @Valid @RequestBody UpdateCompanyRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.toCompany(
                companyService.updateForAdmin(cu.userId(), id, request)));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(HttpServletRequest http,
                                              @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        companyService.deleteForAdmin(cu.userId(), id);
        return ResponseEntity.noContent().build();
    }

    // ---- registrations ----

    /**
     * Paginated registrations for a company. The unpaged {@code listForCompany}
     * was fine for small drives but becomes a problem once registration counts
     * reach the thousands — returning the whole table in one response.
     */
    @GetMapping("/companies/{id}/registrations")
    public ResponseEntity<PageResponse<RegistrationResponse>> listRegistrations(
            HttpServletRequest http,
            @PathVariable @Positive Long id,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        companyService.getScopedToCollege(id, cu.collegeId()); // scope check
        Pageable pageable = PageRequest.of(page, size, Sort.by("registeredAt").ascending());
        return ResponseEntity.ok(PageResponse.of(
                registrationService.listForCompany(id, pageable), EntityMapper::toRegistration));
    }

    @PutMapping("/registrations/{registrationId}/status")
    public ResponseEntity<RegistrationResponse> updateRegistrationStatus(
            HttpServletRequest http,
            @PathVariable @Positive Long registrationId,
            @Valid @RequestBody RegistrationStatusUpdateRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        // Parsing moved into the service; controller stays free of domain validation.
        return ResponseEntity.ok(EntityMapper.toRegistration(
                registrationService.updateStatus(registrationId, request.status(), cu.collegeId())));
    }

    // ---- students ----

    @GetMapping("/students")
    public ResponseEntity<PageResponse<StudentResponse>> listStudents(
            HttpServletRequest http,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        Pageable pageable = PageRequest.of(page, size, Sort.by("usn").ascending());
        return ResponseEntity.ok(PageResponse.of(
                studentService.listByCollege(cu.collegeId(), pageable), EntityMapper::toStudent));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentResponse> getStudent(HttpServletRequest http,
                                                      @PathVariable @Positive Long id) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        Student s = studentService.getById(id);
        if (!s.getCollege().getCollegeId().equals(cu.collegeId())) {
            throw new CrossCollegeAccessException("Student belongs to a different college");
        }
        return ResponseEntity.ok(EntityMapper.toStudent(s));
    }

    // ---- evaluation rounds ----

    /**
     * Paginated list of registered students for a company with their
     * per-round state and audit history. Status and audit rows are batch
     * fetched scoped to the current page, so DB cost grows with page size
     * rather than drive size.
     */
    @GetMapping("/companies/{companyId}/evaluation-rounds")
    public ResponseEntity<CompanyRoundProgressResponse> listEvaluationRounds(
            HttpServletRequest http,
            @PathVariable @Positive Long companyId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        Pageable pageable = PageRequest.of(page, size, Sort.by("registeredAt").ascending());
        return ResponseEntity.ok(
                evaluationRoundService.listForCompany(companyId, cu.collegeId(), pageable));
    }

    @PostMapping("/companies/{companyId}/students/{studentId}/evaluation-rounds")
    public ResponseEntity<StudentRoundProgress> addEvaluationRound(
            HttpServletRequest http,
            @PathVariable @Positive Long companyId,
            @PathVariable @Positive Long studentId,
            @Valid @RequestBody RoundUpdateRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(evaluationRoundService.addRound(
                studentId, companyId, request.round(), cu.userId()));
    }

    @DeleteMapping("/companies/{companyId}/students/{studentId}/evaluation-rounds")
    public ResponseEntity<StudentRoundProgress> removeEvaluationRound(
            HttpServletRequest http,
            @PathVariable @Positive Long companyId,
            @PathVariable @Positive Long studentId,
            @Valid @RequestBody RoundUpdateRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(evaluationRoundService.removeRound(
                studentId, companyId, request.round(), cu.userId()));
    }
}
