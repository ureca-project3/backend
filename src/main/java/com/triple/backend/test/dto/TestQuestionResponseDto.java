package com.triple.backend.test.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TestQuestionResponseDto {

    String name;
    String description;
    List<String> question;

    public TestQuestionResponseDto(String name, String description, List<String> question) {
        this.name = name;
        this.description = description;
        this.question = question;
    }

}
