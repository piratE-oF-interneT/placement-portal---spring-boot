package com.portal.placementportal.entity;

/**
 * NORMAL   — student is fully placed and cannot apply to any further companies.
 * SPECIAL  — student may apply only to companies whose CTC is at least 2x the
 *            CTC of the placement (Tier-2 / dream-offer policy).
 */
public enum PlacementCategory {
    NORMAL,
    SPECIAL
}
