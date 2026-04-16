package com.portal.placementportal.service;

import com.portal.placementportal.dto.PlacementDtos.PlaceStudentRequest;
import com.portal.placementportal.entity.Placement;

import java.util.List;
import java.util.Optional;

public interface PlacementService {

    /** Place a student in a company under the given category. */
    Placement placeStudent(Long adminId, PlaceStudentRequest request);

    List<Placement> listForStudent(Long studentId);

    List<Placement> listForCompany(Long companyId, Long adminCollegeId);

    /**
     * Returns true if the student has any NORMAL placement — they are frozen
     * out of any further applications.
     */
    boolean hasNormalPlacement(Long studentId);

    /**
     * Returns the highest-CTC SPECIAL placement for the student, if any.
     * Used by the dream-offer (2x) rule during application.
     */
    Optional<Placement> highestSpecialPlacement(Long studentId);
}
