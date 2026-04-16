package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.AuthDtos.LoginRequest;
import com.portal.placementportal.dto.AuthDtos.LoginResponse;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Credentials;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.InvalidCredentialsException;
import com.portal.placementportal.repository.AdminUserRepository;
import com.portal.placementportal.repository.StudentRepository;
import com.portal.placementportal.service.AuthService;
import com.portal.placementportal.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticates by looking up Credentials by loginId (USN or admin username),
 * verifying the BCrypt hash, then enriching the response with the domain
 * entity's id / college.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CredentialsService credentialsService;
    private final StudentRepository studentRepository;
    private final AdminUserRepository adminRepository;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Credentials credentials;
        try {
            credentials = credentialsService.getByLoginId(request.username());
        } catch (RuntimeException ex) {
            // Don't leak whether loginId exists
            throw new InvalidCredentialsException();
        }
        credentialsService.verifyPassword(credentials, request.password());

        if (credentials.getRole() == Role.STUDENT) {
            Student student = studentRepository.findByUsnIgnoreCase(credentials.getLoginId())
                    .orElseThrow(InvalidCredentialsException::new);
            return new LoginResponse(
                    student.getStudentId(),
                    student.getUsn(),
                    Role.STUDENT,
                    student.getCollege().getCollegeId(),
                    "login successful"
            );
        } else {
            AdminUser admin = adminRepository.findByUsername(credentials.getLoginId())
                    .orElseThrow(InvalidCredentialsException::new);
            Long collegeId = admin.getCollege() == null ? null : admin.getCollege().getCollegeId();
            return new LoginResponse(
                    admin.getAdminId(),
                    admin.getUsername(),
                    admin.getRole(),
                    collegeId,
                    "login successful"
            );
        }
    }
}
