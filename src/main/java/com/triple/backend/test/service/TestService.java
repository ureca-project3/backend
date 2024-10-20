package com.triple.backend.test.service;

import com.triple.backend.test.dto.TestAnswerRequestDto;
import com.triple.backend.test.dto.TestQuestionResponseDto;
import org.springframework.data.domain.Pageable;


public interface TestService {
    // 자녀 성향 질문 조회
    TestQuestionResponseDto getTestQuestion(Long testId, Pageable pageable);

    // 자녀 성향 진단 결과 등록
    void insertTestResult(Long testId, TestAnswerRequestDto testAnswerRequestDto);

}
