package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Student profile. Authentication material (password) lives in the Credentials
 * entity and is looked up by email / login id.
 */
@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(columnNames = "usn"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @SequenceGenerator(name = "student_seq", sequenceName = "student_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_seq")
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "usn", nullable = false, length = 32)
    private String usn;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 16)
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "branch", length = 64)
    private String branch;

    @Column(name = "batch_year")
    private Integer batchYear;

    // Academic records
    @Column(name = "ssc_percentage")
    private Double sscPercentage;

    @Column(name = "hsc_percentage")
    private Double hscPercentage;

    @Column(name = "cgpa")
    private Double cgpa;

    @Column(name = "sem1_gpa") private Double sem1Gpa;
    @Column(name = "sem2_gpa") private Double sem2Gpa;
    @Column(name = "sem3_gpa") private Double sem3Gpa;
    @Column(name = "sem4_gpa") private Double sem4Gpa;
    @Column(name = "sem5_gpa") private Double sem5Gpa;
    @Column(name = "sem6_gpa") private Double sem6Gpa;
    @Column(name = "sem7_gpa") private Double sem7Gpa;
    @Column(name = "sem8_gpa") private Double sem8Gpa;

    @Column(name = "current_backlogs")
    private Integer currentBacklogs;

    @Column(name = "profile_complete", nullable = false)
    private boolean profileComplete;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;
}
