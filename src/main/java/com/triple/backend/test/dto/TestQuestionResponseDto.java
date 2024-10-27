package com.triple.backend.test.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TestQuestionResponseDto {

    String name;
    String description;
    Map<Long, String> question;

    public TestQuestionResponseDto(String name, String description, Map<Long, String> question) {
        this.name = name;
        this.description = description;
        this.question = question;
    }

}
