package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.AuthDtos.CreateCollegeRequest;
import com.portal.placementportal.entity.College;
import com.portal.placementportal.exception.DuplicateResourceException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.CollegeRepository;
import com.portal.placementportal.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;

    @Override
    @Transactional
    public College create(CreateCollegeRequest request) {
        if (collegeRepository.existsByCollegeNameIgnoreCase(request.collegeName())) {
            throw new DuplicateResourceException("College already exists: " + request.collegeName());
        }
        College c = College.builder()
                .collegeName(request.collegeName())
                .address(request.address())
                .build();
        return collegeRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<College> list() {
        return collegeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public College getById(Long id) {
        return collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College", id));
    }
}
