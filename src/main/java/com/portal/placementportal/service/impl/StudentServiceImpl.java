package com.portal.placementportal.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portal.placementportal.dto.AuthDtos.StudentRegisterRequest;
import com.portal.placementportal.dto.StudentDtos.ChangePasswordRequest;
import com.portal.placementportal.dto.StudentDtos.UpdateProfileRequest;
import com.portal.placementportal.entity.College;
import com.portal.placementportal.entity.Credentials;
import com.portal.placementportal.entity.Role;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.StudentRepository;
import com.portal.placementportal.service.CollegeService;
import com.portal.placementportal.service.CredentialsService;
import com.portal.placementportal.service.MailService;
import com.portal.placementportal.service.StudentService;
import com.portal.placementportal.utilities.PasswordEncoderUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CollegeService collegeService;
    private final CredentialsService credentialsService;
    private final PasswordEncoderUtil passwordEncoder;
    private final MailService mailService;

    /**
     * Self-service student registration. Generates a random password, stores the
     * BCrypt hash in Credentials, and dispatches the raw password by email.
     */
    @Override
    @Transactional
    public Student register(StudentRegisterRequest request) {
        if (studentRepository.existsByUsnIgnoreCase(request.usn())) {
            throw new DuplicateResourceException("USN already registered: " + request.usn());
        }
        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        College college = collegeService.getById(request.collegeId());

        Student student = Student.builder()
                .usn(request.usn())
                .email(request.email())
                .college(college)
                .profileComplete(false)
                .build();
        student = studentRepository.save(student);

        String rawPassword = passwordEncoder.generateRandomPassword(10);
        credentialsService.create(request.usn(), request.email(), rawPassword, Role.STUDENT);
        mailService.sendStudentCredentials(request.email(), request.usn(), rawPassword);
        return student;
    }

    @Override
    @Transactional(readOnly = true)
    public Student getById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> listByCollege(Long collegeId) {
        return studentRepository.findByCollege_CollegeId(collegeId);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Student> listByCollege(
            Long collegeId, org.springframework.data.domain.Pageable pageable) {
        return studentRepository.findByCollege_CollegeId(collegeId, pageable);
    }

    @Override
    @Transactional
    public Student updateProfile(Long studentId, UpdateProfileRequest r) {
        Student s = getById(studentId);
        if (r.fullName() != null)
            s.setFullName(r.fullName());
        if (r.phone() != null)
            s.setPhone(r.phone());
        if (r.dateOfBirth() != null)
            s.setDateOfBirth(r.dateOfBirth());
        if (r.gender() != null)
            s.setGender(r.gender());
        if (r.address() != null)
            s.setAddress(r.address());
        if (r.branch() != null)
            s.setBranch(r.branch());
        if (r.batchYear() != null)
            s.setBatchYear(r.batchYear());
        if (r.sscPercentage() != null)
            s.setSscPercentage(r.sscPercentage());
        if (r.hscPercentage() != null)
            s.setHscPercentage(r.hscPercentage());
        if (r.cgpa() != null)
            s.setCgpa(r.cgpa());
        if (r.sem1Gpa() != null)
            s.setSem1Gpa(r.sem1Gpa());
        if (r.sem2Gpa() != null)
            s.setSem2Gpa(r.sem2Gpa());
        if (r.sem3Gpa() != null)
            s.setSem3Gpa(r.sem3Gpa());
        if (r.sem4Gpa() != null)
            s.setSem4Gpa(r.sem4Gpa());
        if (r.sem5Gpa() != null)
            s.setSem5Gpa(r.sem5Gpa());
        if (r.sem6Gpa() != null)
            s.setSem6Gpa(r.sem6Gpa());
        if (r.sem7Gpa() != null)
            s.setSem7Gpa(r.sem7Gpa());
        if (r.sem8Gpa() != null)
            s.setSem8Gpa(r.sem8Gpa());
        if (r.currentBacklogs() != null)
            s.setCurrentBacklogs(r.currentBacklogs());
        s.setProfileComplete(s.getFullName() != null && s.getBranch() != null && s.getCgpa() != null);
        return s;
    }

    @Override
    @Transactional
    public void changePassword(Long studentId, ChangePasswordRequest request) {
        Student s = getById(studentId);
        Credentials credentials = credentialsService.getByLoginId(s.getUsn());
        credentialsService.verifyPassword(credentials, request.oldPassword());
        credentialsService.updatePassword(credentials, request.newPassword());
    }
}
