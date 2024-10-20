package com.triple.backend.test.service;

import com.triple.backend.test.dto.TestQuestionResponseDto;
import com.triple.backend.test.dto.TestResultDto;
import org.springframework.data.domain.Pageable;


public interface TestService {
    // 자녀 성향 질문 조회
    TestQuestionResponseDto getTestQuestion(Long testId, Pageable pageable);

    // 자녀 성향 진단 결과 조회
    TestResultDto getTestResult(Long childId);
}
