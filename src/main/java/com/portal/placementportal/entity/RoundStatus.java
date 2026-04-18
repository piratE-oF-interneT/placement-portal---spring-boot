package com.portal.placementportal.entity;

/**
 * Outcome of an evaluation round for a (student, company) pair.
 *
 *   PENDING  — round has not been reached / no decision recorded yet.
 *   CLEARED  — student passed this round.
 *   FAILED   — student was rejected at this round.
 *
 * The cascade invariant enforced by the service layer: rounds earlier than
 * a CLEARED round are also CLEARED; rounds later than a FAILED round are
 * always PENDING (the student never got that far).
 */
public enum RoundStatus {
    PENDING,
    CLEARED,
    FAILED
}
