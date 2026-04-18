package com.portal.placementportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Value object stored as the map-value of EvaluationRound#rounds. Holds the
 * current {@link RoundStatus} and the timestamp at which it last changed.
 * Timestamps are updated whenever the status transitions — not on no-op
 * writes — so {@code updatedAt} always reflects the most recent real
 * transition.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundStatusValue {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RoundStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static RoundStatusValue pendingAt(Instant now) {
        return new RoundStatusValue(RoundStatus.PENDING, now);
    }
}
