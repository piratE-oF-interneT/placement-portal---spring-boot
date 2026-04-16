package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Students listed here are barred from applying to any placement drive,
 * regardless of eligibility.
 */
@Entity
@Table(name = "blacklist", uniqueConstraints = {
        @UniqueConstraint(columnNames = "student_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blacklist {

    @Id
    @SequenceGenerator(name = "blacklist_seq", sequenceName = "blacklist_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blacklist_seq")
    @Column(name = "blacklist_id")
    private Long blacklistId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "blacklisted_at", nullable = false)
    private Instant blacklistedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blacklisted_by_admin_id")
    private AdminUser blacklistedBy;
}
