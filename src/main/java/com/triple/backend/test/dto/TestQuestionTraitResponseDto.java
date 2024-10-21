package com.triple.backend.test.dto;

import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.Trait;
import lombok.Getter;

@Getter
public class TestQuestionTraitResponseDto {

    // Question
    Long questionId;
    Test test;
    Trait trait;
    String questionText;

    // Trait
    String traitName;

    public TestQuestionTraitResponseDto(Long questionId, Test test, Trait trait, String questionText, String traitName) {
        this.questionId = questionId;
        this.test = test;
        this.trait = trait;
        this.questionText = questionText;
        this.traitName = traitName;
    }

}
