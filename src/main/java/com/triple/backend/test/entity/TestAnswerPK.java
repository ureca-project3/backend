package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TestAnswerPK implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_participation_id")
    private TestParticipation testParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private TestQuestion testQuestion;

    @Builder
    public TestAnswerPK(TestParticipation testParticipation, TestQuestion testQuestion) {
        this.testParticipation = testParticipation;
        this.testQuestion = testQuestion;
    }

}
