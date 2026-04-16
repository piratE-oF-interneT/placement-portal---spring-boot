package com.portal.placementportal.service;

import com.portal.placementportal.dto.AuthDtos.CreateCollegeRequest;
import com.portal.placementportal.entity.College;

import java.util.List;

public interface CollegeService {
    College create(CreateCollegeRequest request);
    List<College> list();
    College getById(Long id);
}
