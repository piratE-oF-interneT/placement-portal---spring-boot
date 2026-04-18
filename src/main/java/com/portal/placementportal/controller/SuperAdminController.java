package com.portal.placementportal.controller;

import com.portal.placementportal.dto.AuthDtos.CreateAdminRequest;
import com.portal.placementportal.dto.AuthDtos.CreateCollegeRequest;
import com.portal.placementportal.dto.AuthDtos.CreateSuperAdminRequest;
import com.portal.placementportal.dto.EntityMapper;
import com.portal.placementportal.dto.ResponseDtos.AdminUserResponse;
import com.portal.placementportal.dto.ResponseDtos.CollegeResponse;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.security.RequestContext;
import com.portal.placementportal.service.AdminUserService;
import com.portal.placementportal.service.CollegeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final CollegeService collegeService;
    private final AdminUserService adminUserService;
    private final RequestContext requestContext;

    /**
     * Bootstrap endpoint — creates the very first superadmin without a caller
     * identity. Intended to be disabled (or gated by a one-time token) in
     * production once the first superadmin exists.
     */
    @PostMapping("/bootstrap")
    public ResponseEntity<AdminUserResponse> bootstrap(@Valid @RequestBody CreateSuperAdminRequest request) {
        return ResponseEntity.ok(EntityMapper.toAdminUser(
                adminUserService.createSuperAdmin(request)));
    }

    @PostMapping("/superadmins")
    public ResponseEntity<AdminUserResponse> createSuperAdmin(HttpServletRequest http,
                                                              @Valid @RequestBody CreateSuperAdminRequest request) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(EntityMapper.toAdminUser(
                adminUserService.createSuperAdmin(request)));
    }

    @PostMapping("/colleges")
    public ResponseEntity<CollegeResponse> createCollege(HttpServletRequest http,
                                                         @Valid @RequestBody CreateCollegeRequest request) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(EntityMapper.toCollege(collegeService.create(request)));
    }

    @GetMapping("/colleges")
    public ResponseEntity<List<CollegeResponse>> listColleges(HttpServletRequest http) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(EntityMapper.mapList(
                collegeService.list(), EntityMapper::toCollege));
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminUserResponse> createAdmin(HttpServletRequest http,
                                                         @Valid @RequestBody CreateAdminRequest request) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(EntityMapper.toAdminUser(
                adminUserService.createAdmin(request)));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminUserResponse>> listAdmins(HttpServletRequest http) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(EntityMapper.mapList(
                adminUserService.listAllAdmins(), EntityMapper::toAdminUser));
    }
}
