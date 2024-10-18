package com.triple.backend.test.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Data
public class TestAnswerRequestDto {

    List<Map<Long, Integer>> answerList;

}
