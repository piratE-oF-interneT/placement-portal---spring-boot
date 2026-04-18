package com.portal.placementportal.service;

import com.portal.placementportal.dto.AuthDtos.StudentRegisterRequest;
import com.portal.placementportal.dto.StudentDtos.ChangePasswordRequest;
import com.portal.placementportal.dto.StudentDtos.UpdateProfileRequest;
import com.portal.placementportal.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    Student register(StudentRegisterRequest request);
    Student getById(Long id);
    List<Student> listByCollege(Long collegeId);
    /** Paginated variant for admin screens — college rosters can be large. */
    Page<Student> listByCollege(Long collegeId, Pageable pageable);
    Student updateProfile(Long studentId, UpdateProfileRequest request);
    void changePassword(Long studentId, ChangePasswordRequest request);
}
