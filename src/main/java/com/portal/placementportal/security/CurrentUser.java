package com.portal.placementportal.security;

import com.portal.placementportal.entity.Role;

/**
 * Identity of the caller for the current request.
 * collegeId may be null for SUPERADMIN.
 */
public record CurrentUser(Long userId, Role role, Long collegeId) {}
