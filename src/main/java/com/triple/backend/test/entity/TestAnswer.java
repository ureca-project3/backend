package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class TestAnswer {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_participation_id")
    private TestParticipation testParticipation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private TestQuestion testQuestion;

    private Integer answerText;

}
