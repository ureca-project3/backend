package com.triple.backend.test.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TestAnswerRequestDto {

    List<Map<Long, Integer>> answerList;

    public TestAnswerRequestDto(List<Map<Long, Integer>> answerList) {
        this.answerList = answerList;
    }

}
