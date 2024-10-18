package com.triple.backend.test.service;

import com.triple.backend.test.dto.TestQuestionResponseDto;

public interface TestService {
    TestQuestionResponseDto getTestQuestion(Long testId);
}
