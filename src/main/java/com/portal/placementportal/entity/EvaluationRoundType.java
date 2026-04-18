package com.portal.placementportal.entity;

/**
 * Ordered list of the rounds a student can progress through for a single
 * company drive. Declaration order is significant — it defines the pipeline
 * from the first round (SCREENING) to the last (HR), and is the basis for
 * the cascade logic in EvaluationRoundService:
 *   marking a round cleared   → every earlier round is also marked cleared
 *   unmarking (removing) one  → every later round is also unmarked
 */
public enum EvaluationRoundType {
    SCREENING,
    ONLINE_ASSESSMENT,
    TECH1,
    TECH2,
    TECH3,
    MANAGERIAL,
    HR
}
