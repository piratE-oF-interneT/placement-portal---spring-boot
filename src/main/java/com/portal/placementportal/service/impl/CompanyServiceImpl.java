package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.CompanyDtos.CreateCompanyRequest;
import com.portal.placementportal.dto.CompanyDtos.UpdateCompanyRequest;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Company;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.exception.ForbiddenOperationException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.CompanyRepository;
import com.portal.placementportal.service.AdminUserService;
import com.portal.placementportal.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final AdminUserService adminUserService;

    @Override
    @Transactional
    public Company createForAdmin(Long adminId, CreateCompanyRequest request) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Company c = Company.builder()
                .name(request.name())
                .description(request.description())
                .roleOffered(request.roleOffered())
                .ctcLpa(request.ctcLpa())
                .location(request.location())
                .minCgpa(request.minCgpa())
                .minSscPercentage(request.minSscPercentage())
                .minHscPercentage(request.minHscPercentage())
                .maxBacklogs(request.maxBacklogs())
                .eligibleBranches(request.eligibleBranches())
                .driveDate(request.driveDate())
                .registrationDeadline(request.registrationDeadline())
                .active(true)
                .college(admin.getCollege())
                .createdBy(admin)
                .build();
        return companyRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Company> listForCollege(Long collegeId) {
        return companyRepository.findByCollege_CollegeId(collegeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Company> listActiveForCollege(Long collegeId) {
        return companyRepository.findByCollege_CollegeIdAndActiveTrue(collegeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Company getScopedToCollege(Long companyId, Long collegeId) {
        Company c = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        if (!c.getCollege().getCollegeId().equals(collegeId)) {
            throw new CrossCollegeAccessException("Company belongs to a different college");
        }
        return c;
    }

    @Override
    @Transactional
    public Company updateForAdmin(Long adminId, Long companyId, UpdateCompanyRequest r) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Company c = getScopedToCollege(companyId, admin.getCollege().getCollegeId());
        if (r.description() != null) c.setDescription(r.description());
        if (r.roleOffered() != null) c.setRoleOffered(r.roleOffered());
        if (r.ctcLpa() != null) c.setCtcLpa(r.ctcLpa());
        if (r.location() != null) c.setLocation(r.location());
        if (r.minCgpa() != null) c.setMinCgpa(r.minCgpa());
        if (r.minSscPercentage() != null) c.setMinSscPercentage(r.minSscPercentage());
        if (r.minHscPercentage() != null) c.setMinHscPercentage(r.minHscPercentage());
        if (r.maxBacklogs() != null) c.setMaxBacklogs(r.maxBacklogs());
        if (r.eligibleBranches() != null) c.setEligibleBranches(r.eligibleBranches());
        if (r.driveDate() != null) c.setDriveDate(r.driveDate());
        if (r.registrationDeadline() != null) c.setRegistrationDeadline(r.registrationDeadline());
        if (r.active() != null) c.setActive(r.active());
        return c;
    }

    @Override
    @Transactional
    public void deleteForAdmin(Long adminId, Long companyId) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Company c = getScopedToCollege(companyId, admin.getCollege().getCollegeId());
        companyRepository.delete(c);
    }
}
