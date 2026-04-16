package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @SequenceGenerator(name = "company_seq", sequenceName = "company_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_seq")
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "role_offered")
    private String roleOffered;

    @Column(name = "ctc_lpa")
    private Double ctcLpa;

    @Column(name = "location")
    private String location;

    @Column(name = "min_cgpa")
    private Double minCgpa;

    @Column(name = "min_ssc_percentage")
    private Double minSscPercentage;

    @Column(name = "min_hsc_percentage")
    private Double minHscPercentage;

    @Column(name = "max_backlogs")
    private Integer maxBacklogs;

    @Column(name = "eligible_branches")
    private String eligibleBranches; // CSV, e.g. "CSE,ISE,ECE"

    @Column(name = "drive_date")
    private LocalDate driveDate;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private AdminUser createdBy;
}
