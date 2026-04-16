package com.portal.placementportal.service;

import com.portal.placementportal.dto.AuthDtos.StudentRegisterRequest;
import com.portal.placementportal.dto.StudentDtos.ChangePasswordRequest;
import com.portal.placementportal.dto.StudentDtos.UpdateProfileRequest;
import com.portal.placementportal.entity.Student;

import java.util.List;

public interface StudentService {
    Student register(StudentRegisterRequest request);
    Student getById(Long id);
    List<Student> listByCollege(Long collegeId);
    Student updateProfile(Long studentId, UpdateProfileRequest request);
    void changePassword(Long studentId, ChangePasswordRequest request);
}
