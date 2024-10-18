package com.triple.backend.test.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestQuestionRequestDto {

    String name;
    String description;
    List<String> question;

}
