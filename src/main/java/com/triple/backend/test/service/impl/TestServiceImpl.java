package com.triple.backend.test.service.impl;

import com.triple.backend.test.dto.TestQuestionResponseDto;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestQuestion;
import com.triple.backend.test.repository.TestQuestionRepository;
import com.triple.backend.test.repository.TestRepository;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;

    private final TestQuestionRepository testQuestionRepository;

    // 자녀 성향 질문 조회
    @Override
    public TestQuestionResponseDto getTestQuestion(Long testId, Pageable pageable) {

        Test test = testRepository.findById(testId).orElseThrow( () -> new IllegalArgumentException("테스트 정보를 찾을 수 없습니다."));

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTest(test, pageable);

        List<Map<Long, String>> questionList = new ArrayList<>();

        for (TestQuestion testQuestion : testQuestionList) {
            Map<Long, String> questionMap = new HashMap<>();
            questionMap.put(testQuestion.getQuestionId(), testQuestion.getQuestionText());
            questionList.add(questionMap);
        }

        return new TestQuestionResponseDto(test.getName(), test.getDescription(), questionList);
    }

}
