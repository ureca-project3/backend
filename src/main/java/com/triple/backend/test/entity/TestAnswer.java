package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TestAnswer {

    @EmbeddedId
    TestAnswerPK testAnswerPK;

    private Integer answerText;

    @Builder
    public TestAnswer(TestAnswerPK testAnswerPK, Integer answerText) {
        this.testAnswerPK = testAnswerPK;
        this.answerText = answerText;
    }
}
