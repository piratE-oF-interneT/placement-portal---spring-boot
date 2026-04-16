package com.portal.placementportal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colleges", uniqueConstraints = @UniqueConstraint(columnNames = "college_name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class College {

    @Id
    @SequenceGenerator(name = "college_seq", sequenceName = "college_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "college_seq")
    @Column(name = "college_id")
    private Long collegeId;

    @Column(name = "college_name", nullable = false)
    private String collegeName;

    @Column(name = "address")
    private String address;
}
