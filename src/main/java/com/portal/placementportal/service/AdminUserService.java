package com.portal.placementportal.service;

import com.portal.placementportal.dto.AuthDtos.CreateAdminRequest;
import com.portal.placementportal.dto.AuthDtos.CreateSuperAdminRequest;
import com.portal.placementportal.entity.AdminUser;

import java.util.List;

public interface AdminUserService {
    AdminUser createAdmin(CreateAdminRequest request);
    AdminUser createSuperAdmin(CreateSuperAdminRequest request);
    List<AdminUser> listAdminsForCollege(Long collegeId);
    List<AdminUser> listAllAdmins();
    AdminUser getById(Long id);
}
