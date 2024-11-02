package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Trait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long traitId;

    private String traitDescription;

    private String traitName;

    private Integer maxScore;

    private Integer minScore;

    @JoinColumn(name = "test_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Test test;

    @Builder
    public Trait (String traitName, String traitDescription, Integer maxScore, Integer minScore, Test test) {
        this.traitName = traitName;
        this.traitDescription = traitDescription;
        this.maxScore = maxScore;
        this.minScore = minScore;
        this.test = test;
    }
}
