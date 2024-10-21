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
    TestAnswerId testAnswerId;

    private Integer answerText;

    @Builder
    public TestAnswer(TestAnswerId testAnswerId, Integer answerText) {
        this.testAnswerId = testAnswerId;
        this.answerText = answerText;
    }
}
