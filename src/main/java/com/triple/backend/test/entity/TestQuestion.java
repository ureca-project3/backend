package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test;

    @ManyToOne
    @JoinColumn(name = "trait_id")
    private Trait trait;

    @Column(name = "question_text")
    private String questionText;
}
