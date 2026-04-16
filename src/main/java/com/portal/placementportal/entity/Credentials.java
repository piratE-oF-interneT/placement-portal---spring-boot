package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Authentication credentials for any portal user (student / admin / superadmin).
 * Decouples the authentication material (email + hashed password) from the
 * domain entities (Student / AdminUser) so that account recovery, rotation and
 * role changes can be handled without touching domain tables.
 *
 * The linkage to a domain entity is indirect: Student.email and AdminUser.email
 * are unique, and each carries the same email that appears in Credentials.
 */
@Entity
@Table(name = "credentials", uniqueConstraints = {
        @UniqueConstraint(name = "uk_credentials_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_credentials_login_id", columnNames = "login_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credentials {

    @Id
    @SequenceGenerator(name = "credentials_seq", sequenceName = "credentials_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credentials_seq")
    @Column(name = "credentials_id")
    private Long credentialsId;

    /**
     * The identifier a user types to log in: USN for STUDENT, username for
     * ADMIN/SUPERADMIN. Stored separately from email so neither has to change
     * when the user updates the other.
     */
    @Column(name = "login_id", nullable = false, length = 64)
    private String loginId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** BCrypt-encoded password — never store plain text here. */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Role role;

    @Column(name = "active", nullable = false)
    private boolean active;
}
