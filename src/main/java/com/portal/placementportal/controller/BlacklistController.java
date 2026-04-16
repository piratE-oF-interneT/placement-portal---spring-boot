package com.portal.placementportal.controller;

import com.portal.placementportal.dto.BlacklistDtos.BlacklistStudentRequest;
import com.portal.placementportal.entity.Blacklist;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.security.CurrentUser;
import com.portal.placementportal.security.RequestContext;
import com.portal.placementportal.service.BlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/blacklist")
@RequiredArgsConstructor
@Validated
public class BlacklistController {

    private final BlacklistService blacklistService;
    private final RequestContext requestContext;

    @PostMapping
    public ResponseEntity<Blacklist> blacklist(HttpServletRequest http,
                                               @Valid @RequestBody BlacklistStudentRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(blacklistService.add(cu.userId(), request));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> remove(HttpServletRequest http, @PathVariable @Positive Long studentId) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        blacklistService.remove(cu.userId(), studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Blacklist>> list(HttpServletRequest http) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(blacklistService.listForCollege(cu.collegeId()));
    }
}
