package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Immutable audit row capturing a single round-status transition. Written
 * by the service layer on every real state change (no-op updates produce
 * no audit rows). Admin username is snapshotted at write-time so history
 * remains interpretable even if the admin record is later renamed or
 * deleted.
 */
@Entity
@Table(name = "evaluation_round_audits", indexes = {
        @Index(name = "ix_era_student_company",
                columnList = "student_id,company_id,changed_at"),
        @Index(name = "ix_era_company", columnList = "company_id,changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationRoundAudit {

    @Id
    @SequenceGenerator(name = "evaluation_round_audit_seq",
            sequenceName = "evaluation_round_audit_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "evaluation_round_audit_seq")
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 32)
    private EvaluationRoundType roundType;

    /** Nullable: null means "row did not exist before this change". */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 16)
    private RoundStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 16)
    private RoundStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_admin_id", nullable = false)
    private AdminUser changedBy;

    /**
     * Snapshot of the admin's username at the time of change, so audit
     * records remain readable independently of the admin row.
     */
    @Column(name = "changed_by_username", nullable = false, length = 64)
    private String changedByUsername;
}
