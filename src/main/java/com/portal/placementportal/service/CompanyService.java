package com.portal.placementportal.service;

import com.portal.placementportal.dto.CompanyDtos.CreateCompanyRequest;
import com.portal.placementportal.dto.CompanyDtos.UpdateCompanyRequest;
import com.portal.placementportal.entity.Company;

import java.util.List;

public interface CompanyService {
    Company createForAdmin(Long adminId, CreateCompanyRequest request);
    List<Company> listForCollege(Long collegeId);
    List<Company> listActiveForCollege(Long collegeId);
    Company getScopedToCollege(Long companyId, Long collegeId);
    Company updateForAdmin(Long adminId, Long companyId, UpdateCompanyRequest request);
    void deleteForAdmin(Long adminId, Long companyId);
}
