package com.portal.placementportal.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite primary key for {@link EvaluationRound}. The field names
 * (student, company) must match the names of the {@code @Id}-annotated
 * association fields on the entity, and the types must match the PK type
 * of the referenced entities (Long for both Student and Company).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EvaluationRoundId implements Serializable {

    private Long student;
    private Long company;
}
