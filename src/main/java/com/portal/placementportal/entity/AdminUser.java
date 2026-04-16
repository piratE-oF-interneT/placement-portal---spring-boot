package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents both ADMIN (college-scoped) and SUPERADMIN (university-wide) users.
 * For SUPERADMIN, college may be null. Authentication material lives in the
 * Credentials entity — this table stores the profile only.
 */
@Entity
@Table(name = "admin_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {

    @Id
    @SequenceGenerator(name = "admin_user_seq", sequenceName = "admin_user_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_user_seq")
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // null when role = SUPERADMIN
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;
}
