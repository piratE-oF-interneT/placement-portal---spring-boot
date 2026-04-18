package com.portal.placementportal.controller;

import com.portal.placementportal.dto.EntityMapper;
import com.portal.placementportal.dto.PlacementDtos.PlaceStudentRequest;
import com.portal.placementportal.dto.ResponseDtos.PlacementResponse;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.security.CurrentUser;
import com.portal.placementportal.security.RequestContext;
import com.portal.placementportal.service.PlacementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints for recording placement outcomes. Responses are
 * always {@link PlacementResponse} DTOs, never Placement entities.
 */
@RestController
@RequestMapping("/api/admin/placements")
@RequiredArgsConstructor
@Validated
public class PlacementController {

    private final PlacementService placementService;
    private final RequestContext requestContext;

    @PostMapping
    public ResponseEntity<PlacementResponse> placeStudent(HttpServletRequest http,
                                                          @Valid @RequestBody PlaceStudentRequest request) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.toPlacement(
                placementService.placeStudent(cu.userId(), request)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PlacementResponse>> listForStudent(HttpServletRequest http,
                                                                  @PathVariable @Positive Long studentId) {
        requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.mapList(
                placementService.listForStudent(studentId), EntityMapper::toPlacement));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<PlacementResponse>> listForCompany(HttpServletRequest http,
                                                                  @PathVariable @Positive Long companyId) {
        CurrentUser cu = requestContext.requireRole(http, Role.ADMIN);
        return ResponseEntity.ok(EntityMapper.mapList(
                placementService.listForCompany(companyId, cu.collegeId()), EntityMapper::toPlacement));
    }
}
