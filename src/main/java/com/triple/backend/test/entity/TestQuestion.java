package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
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

    @Builder
    public TestQuestion(Long questionId, Test test, Trait trait, String questionText) {
        this.questionId = questionId;
        this.test = test;
        this.trait = trait;
        this.questionText = questionText;
    }
}
