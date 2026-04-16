package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.BlacklistDtos.BlacklistStudentRequest;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Blacklist;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.ForbiddenOperationException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.BlacklistRepository;
import com.portal.placementportal.service.AdminUserService;
import com.portal.placementportal.service.BlacklistService;
import com.portal.placementportal.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final StudentService studentService;
    private final AdminUserService adminUserService;

    @Override
    @Transactional
    public Blacklist add(Long adminId, BlacklistStudentRequest request) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Student student = studentService.getById(request.studentId());
        if (!student.getCollege().getCollegeId().equals(admin.getCollege().getCollegeId())) {
            throw new CrossCollegeAccessException("Student belongs to a different college");
        }
        if (blacklistRepository.existsByStudent_StudentId(student.getStudentId())) {
            throw new DuplicateResourceException("Student is already blacklisted");
        }
        Blacklist b = Blacklist.builder()
                .student(student)
                .reason(request.reason())
                .blacklistedAt(Instant.now())
                .blacklistedBy(admin)
                .build();
        return blacklistRepository.save(b);
    }

    @Override
    @Transactional
    public void remove(Long adminId, Long studentId) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Blacklist b = blacklistRepository.findByStudent_StudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Blacklist entry for student", studentId));
        if (!b.getStudent().getCollege().getCollegeId().equals(admin.getCollege().getCollegeId())) {
            throw new CrossCollegeAccessException("Blacklist entry belongs to a different college");
        }
        blacklistRepository.delete(b);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(Long studentId) {
        return blacklistRepository.existsByStudent_StudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Blacklist> listForCollege(Long collegeId) {
        return blacklistRepository.findByStudent_College_CollegeId(collegeId);
    }
}
