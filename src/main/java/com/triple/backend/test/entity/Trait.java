package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
}
