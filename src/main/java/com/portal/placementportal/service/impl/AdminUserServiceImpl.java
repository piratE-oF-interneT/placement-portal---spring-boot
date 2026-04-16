package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.AuthDtos.CreateAdminRequest;
import com.portal.placementportal.dto.AuthDtos.CreateSuperAdminRequest;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.College;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.AdminUserRepository;
import com.portal.placementportal.service.AdminUserService;
import com.portal.placementportal.service.CollegeService;
import com.portal.placementportal.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminRepository;
    private final CollegeService collegeService;
    private final CredentialsService credentialsService;

    @Override
    @Transactional
    public AdminUser createAdmin(CreateAdminRequest request) {
        checkUniqueness(request.username(), request.email());
        College college = collegeService.getById(request.collegeId());
        AdminUser admin = AdminUser.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .role(Role.ADMIN)
                .college(college)
                .build();
        admin = adminRepository.save(admin);
        credentialsService.create(request.username(), request.email(), request.password(), Role.ADMIN);
        return admin;
    }

    @Override
    @Transactional
    public AdminUser createSuperAdmin(CreateSuperAdminRequest request) {
        checkUniqueness(request.username(), request.email());
        AdminUser admin = AdminUser.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .role(Role.SUPERADMIN)
                .college(null)
                .build();
        admin = adminRepository.save(admin);
        credentialsService.create(request.username(), request.email(), request.password(), Role.SUPERADMIN);
        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUser> listAdminsForCollege(Long collegeId) {
        return adminRepository.findByCollege_CollegeIdAndRole(collegeId, Role.ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUser> listAllAdmins() {
        return adminRepository.findByRole(Role.ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUser getById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdminUser", id));
    }

    private void checkUniqueness(String username, String email) {
        if (adminRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already taken: " + username);
        }
        if (adminRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }
    }
}
