package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Records that a student has been placed in a company. A student may have
 * multiple rows only if earlier placements were SPECIAL (dream-offer policy);
 * a NORMAL placement freezes the student out of further drives.
 */
@Entity
@Table(name = "placements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "company_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Placement {

    @Id
    @SequenceGenerator(name = "placement_seq", sequenceName = "placement_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "placement_seq")
    @Column(name = "placement_id")
    private Long placementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 16)
    private PlacementCategory category;

    /** Snapshot of CTC at the time of placement (company CTC can change later). */
    @Column(name = "ctc_lpa", nullable = false)
    private Double ctcLpa;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placed_by_admin_id")
    private AdminUser placedBy;
}
