package com.portal.placementportal.service;

import com.portal.placementportal.dto.BlacklistDtos.BlacklistStudentRequest;
import com.portal.placementportal.entity.Blacklist;

import java.util.List;

public interface BlacklistService {
    Blacklist add(Long adminId, BlacklistStudentRequest request);
    void remove(Long adminId, Long studentId);
    boolean isBlacklisted(Long studentId);
    List<Blacklist> listForCollege(Long collegeId);
}
