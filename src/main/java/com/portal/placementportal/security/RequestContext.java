package com.portal.placementportal.security;

import com.portal.placementportal.entity.Role;
import com.portal.placementportal.exception.ForbiddenOperationException;
import com.portal.placementportal.exception.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Minimal, header-based request context used to enforce role / college scoping
 * until Spring Security / JWT is wired in. Callers are identified via:
 *   X-User-Id     numeric id of the authenticated user
 *   X-Role        one of SUPERADMIN, ADMIN, STUDENT
 *   X-College-Id  college id (required for ADMIN and STUDENT)
 *
 * When JWT is introduced later, only this class needs to change.
 */
@Component
public class RequestContext {

    public CurrentUser require(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        String roleStr = request.getHeader("X-Role");
        String collegeStr = request.getHeader("X-College-Id");

        if (userIdStr == null || roleStr == null) {
            throw new UnauthorizedAccessException("Missing X-User-Id or X-Role header");
        }
        Role role;
        try {
            role = Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Invalid role: " + roleStr);
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdStr.trim());
        } catch (NumberFormatException e) {
            throw new UnauthorizedAccessException("Invalid user id");
        }
        Long collegeId = null;
        if (collegeStr != null && !collegeStr.isBlank()) {
            try {
                collegeId = Long.parseLong(collegeStr.trim());
            } catch (NumberFormatException e) {
                throw new UnauthorizedAccessException("Invalid college id");
            }
        }
        if ((role == Role.ADMIN || role == Role.STUDENT) && collegeId == null) {
            throw new UnauthorizedAccessException("X-College-Id header is required for role " + role);
        }
        return new CurrentUser(userId, role, collegeId);
    }

    public CurrentUser requireRole(HttpServletRequest request, Role... allowed) {
        CurrentUser cu = require(request);
        for (Role r : allowed) {
            if (cu.role() == r) return cu;
        }
        throw new ForbiddenOperationException("Role " + cu.role() + " is not permitted here");
    }
}
