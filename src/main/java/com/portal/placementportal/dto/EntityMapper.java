package com.portal.placementportal.dto;

import com.portal.placementportal.dto.ResponseDtos.AdminUserResponse;
import com.portal.placementportal.dto.ResponseDtos.BlacklistResponse;
import com.portal.placementportal.dto.ResponseDtos.CollegeResponse;
import com.portal.placementportal.dto.ResponseDtos.CompanyResponse;
import com.portal.placementportal.dto.ResponseDtos.PlacementResponse;
import com.portal.placementportal.dto.ResponseDtos.RegistrationResponse;
import com.portal.placementportal.dto.ResponseDtos.StudentResponse;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Blacklist;
import com.portal.placementportal.entity.College;
import com.portal.placementportal.entity.Company;
import com.portal.placementportal.entity.Placement;
import com.portal.placementportal.entity.Registration;
import com.portal.placementportal.entity.Student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Central entity → response-DTO mapper. Static, pure-function style: no
 * hidden state, no DB access, and therefore safe to call from anywhere
 * (services, tests). Mappers only touch direct associations that are
 * already loaded — they never traverse lazy proxies into new queries.
 *
 * When mapping collections, prefer {@link #mapList(Collection, Function)}
 * over calling the mapper inside a stream yourself: it preallocates the
 * result list to the source size, avoiding resize copies for large pages.
 */
public final class EntityMapper {

    private EntityMapper() {}

    /**
     * Map a collection to a list using the given mapper. Sized for the
     * source to avoid ArrayList growth reallocations on larger pages.
     */
    public static <E, R> List<R> mapList(Collection<E> source, Function<E, R> mapper) {
        if (source == null || source.isEmpty()) return List.of();
        List<R> out = new ArrayList<>(source.size());
        for (E e : source) out.add(mapper.apply(e));
        return out;
    }

    // ---- Company ----

    public static CompanyResponse toCompany(Company c) {
        if (c == null) return null;
        return new CompanyResponse(
                c.getCompanyId(),
                c.getName(),
                c.getDescription(),
                c.getRoleOffered(),
                c.getCtcLpa(),
                c.getLocation(),
                c.getMinCgpa(),
                c.getMinSscPercentage(),
                c.getMinHscPercentage(),
                c.getMaxBacklogs(),
                c.getEligibleBranches(),
                c.getDriveDate(),
                c.getRegistrationDeadline(),
                c.isActive(),
                c.getCollege() != null ? c.getCollege().getCollegeId() : null,
                c.getCreatedBy() != null ? c.getCreatedBy().getAdminId() : null
        );
    }

    // ---- Registration ----

    public static RegistrationResponse toRegistration(Registration r) {
        if (r == null) return null;
        Student s = r.getStudent();
        Company c = r.getCompany();
        return new RegistrationResponse(
                r.getRegistrationId(),
                s != null ? s.getStudentId() : null,
                s != null ? s.getUsn() : null,
                s != null ? s.getFullName() : null,
                c != null ? c.getCompanyId() : null,
                c != null ? c.getName() : null,
                r.getStatus(),
                r.getRegisteredAt(),
                r.getNotes()
        );
    }

    // ---- Student ----

    public static StudentResponse toStudent(Student s) {
        if (s == null) return null;
        return new StudentResponse(
                s.getStudentId(),
                s.getUsn(),
                s.getEmail(),
                s.getFullName(),
                s.getPhone(),
                s.getDateOfBirth(),
                s.getGender(),
                s.getAddress(),
                s.getBranch(),
                s.getBatchYear(),
                s.getSscPercentage(),
                s.getHscPercentage(),
                s.getCgpa(),
                s.getSem1Gpa(), s.getSem2Gpa(), s.getSem3Gpa(), s.getSem4Gpa(),
                s.getSem5Gpa(), s.getSem6Gpa(), s.getSem7Gpa(), s.getSem8Gpa(),
                s.getCurrentBacklogs(),
                s.isProfileComplete(),
                s.getCollege() != null ? s.getCollege().getCollegeId() : null
        );
    }

    // ---- Placement ----

    public static PlacementResponse toPlacement(Placement p) {
        if (p == null) return null;
        Student s = p.getStudent();
        Company c = p.getCompany();
        return new PlacementResponse(
                p.getPlacementId(),
                s != null ? s.getStudentId() : null,
                s != null ? s.getUsn() : null,
                c != null ? c.getCompanyId() : null,
                c != null ? c.getName() : null,
                p.getCategory(),
                p.getCtcLpa(),
                p.getPlacedAt(),
                p.getPlacedBy() != null ? p.getPlacedBy().getAdminId() : null
        );
    }

    // ---- Blacklist ----

    public static BlacklistResponse toBlacklist(Blacklist b) {
        if (b == null) return null;
        Student s = b.getStudent();
        return new BlacklistResponse(
                b.getBlacklistId(),
                s != null ? s.getStudentId() : null,
                s != null ? s.getUsn() : null,
                b.getReason(),
                b.getBlacklistedAt(),
                b.getBlacklistedBy() != null ? b.getBlacklistedBy().getAdminId() : null
        );
    }

    // ---- College ----

    public static CollegeResponse toCollege(College c) {
        if (c == null) return null;
        return new CollegeResponse(c.getCollegeId(), c.getCollegeName(), c.getAddress());
    }

    // ---- AdminUser ----

    public static AdminUserResponse toAdminUser(AdminUser a) {
        if (a == null) return null;
        return new AdminUserResponse(
                a.getAdminId(),
                a.getUsername(),
                a.getFullName(),
                a.getEmail(),
                a.getRole(),
                a.getCollege() != null ? a.getCollege().getCollegeId() : null
        );
    }
}
