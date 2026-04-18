package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Aggregate tracking row for a single (student, company) pair. The primary
 * key is composite (studentId, companyId), so at most one aggregate exists
 * per pair. The per-round status+timestamp is stored in a child collection
 * table keyed by round type, which keeps this entity extensible if new
 * round types are ever added.
 *
 * Concurrency: the {@code @Version} field drives optimistic locking on
 * updates, and the repository exposes a pessimistic-write lock variant
 * used by the service during read-modify-write operations.
 */
@Entity
@Table(name = "evaluation_rounds")
@IdClass(EvaluationRoundId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationRound {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Optimistic-locking token. JPA increments it on every flush. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Per-round status + timestamp. Stored in evaluation_round_statuses
     * with (student_id, company_id, round_type) as the row key. Always
     * populated for every round type by the service layer so callers can
     * rely on the map being dense.
     */
    /**
     * Per-round status + timestamp. Stored in evaluation_round_statuses
     * with (student_id, company_id, round_type) as the row key.
     *
     * Fetch strategy: LAZY + {@code @BatchSize}. On a listing request we
     * intentionally issue a JPQL {@code JOIN FETCH} so the collection is
     * pre-populated for the whole page in one round-trip; the batch-size
     * hint is a safety net for any access path that does not pre-fetch
     * (e.g. navigating a single aggregate after a mutation), collapsing
     * what would be an N-query burst into a single IN-list query.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "evaluation_round_statuses",
            joinColumns = {
                    @JoinColumn(name = "student_id", referencedColumnName = "student_id"),
                    @JoinColumn(name = "company_id", referencedColumnName = "company_id")
            }
    )
    @MapKeyColumn(name = "round_type", length = 32)
    @MapKeyEnumerated(EnumType.STRING)
    @BatchSize(size = 64)
    @Builder.Default
    private Map<EvaluationRoundType, RoundStatusValue> rounds =
            new EnumMap<>(EvaluationRoundType.class);

    /**
     * Initialise every round to PENDING. Used the first time a tracking
     * row is created for a (student, company) pair.
     */
    public void seedPending(Instant now) {
        if (rounds == null) {
            rounds = new EnumMap<>(EvaluationRoundType.class);
        }
        for (EvaluationRoundType t : EvaluationRoundType.values()) {
            rounds.put(t, RoundStatusValue.pendingAt(now));
        }
    }

    public RoundStatus statusOf(EvaluationRoundType type) {
        RoundStatusValue v = rounds.get(type);
        return v == null ? RoundStatus.PENDING : v.getStatus();
    }

    /**
     * Transition one round to a new status. Returns true if the status
     * actually changed (i.e. an audit row should be written). The
     * timestamp is refreshed only on real transitions so {@code
     * updatedAt} reflects the last real change, not a no-op write.
     */
    public boolean transition(EvaluationRoundType type, RoundStatus newStatus, Instant now) {
        RoundStatusValue v = rounds.get(type);
        if (v == null) {
            rounds.put(type, new RoundStatusValue(newStatus, now));
            return true;
        }
        if (v.getStatus() == newStatus) {
            return false;
        }
        v.setStatus(newStatus);
        v.setUpdatedAt(now);
        return true;
    }
}
