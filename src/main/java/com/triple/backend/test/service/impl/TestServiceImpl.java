package com.triple.backend.test.service.impl;

import com.triple.backend.test.dto.TestQuestionResponseDto;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestQuestion;
import com.triple.backend.test.repository.TestQuestionRepository;
import com.triple.backend.test.repository.TestRepository;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

     private final TestRepository testRepository;

     private final TestQuestionRepository testQuestionRepository;

    @Override
    public TestQuestionResponseDto getTestQuestion(Long testId) {

        Test test = testRepository.findById(testId).orElseThrow( () -> new IllegalArgumentException("테스트 정보를 찾을 수 없습니다."));

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTest(test);

        List<String> questionList = new ArrayList<>();

        for (TestQuestion testQuestion : testQuestionList) {
            questionList.add(testQuestion.getQuestionText());
        }

        return new TestQuestionResponseDto(test.getName(), test.getDescription(), questionList);
    }

}
