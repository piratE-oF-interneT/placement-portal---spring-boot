package com.portal.placementportal.controller;

import com.portal.placementportal.dto.AuthDtos.CreateAdminRequest;
import com.portal.placementportal.dto.AuthDtos.CreateCollegeRequest;
import com.portal.placementportal.dto.AuthDtos.CreateSuperAdminRequest;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.College;
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
    public ResponseEntity<AdminUser> bootstrap(@Valid @RequestBody CreateSuperAdminRequest request) {
        return ResponseEntity.ok(adminUserService.createSuperAdmin(request));
    }

    @PostMapping("/superadmins")
    public ResponseEntity<AdminUser> createSuperAdmin(HttpServletRequest http,
                                                      @Valid @RequestBody CreateSuperAdminRequest request) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(adminUserService.createSuperAdmin(request));
    }

    @PostMapping("/colleges")
    public ResponseEntity<College> createCollege(HttpServletRequest http,
                                                 @Valid @RequestBody CreateCollegeRequest request) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(collegeService.create(request));
    }

    @GetMapping("/colleges")
    public ResponseEntity<List<College>> listColleges(HttpServletRequest http) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(collegeService.list());
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminUser> createAdmin(HttpServletRequest http,
                                                 @Valid @RequestBody CreateAdminRequest request) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(adminUserService.createAdmin(request));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminUser>> listAdmins(HttpServletRequest http) {
        requestContext.requireRole(http, Role.SUPERADMIN);
        return ResponseEntity.ok(adminUserService.listAllAdmins());
    }
}
