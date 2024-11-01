package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode
public class TestAnswerId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_participation_id")
    private TestParticipation testParticipation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private TestQuestion testQuestion;

    public TestAnswerId(TestParticipation testParticipation, TestQuestion testQuestion) {
        this.testParticipation = testParticipation;
        this.testQuestion = testQuestion;
    }

}
