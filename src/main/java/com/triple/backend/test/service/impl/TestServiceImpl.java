package com.triple.backend.test.service.impl;

import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.test.dto.TestParticipationRequestDto;
import com.triple.backend.test.dto.TestQuestionResponseDto;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestParticipation;
import com.triple.backend.test.entity.TestQuestion;
import com.triple.backend.test.repository.TestParticipationRepository;
import com.triple.backend.test.repository.TestQuestionRepository;
import com.triple.backend.test.repository.TestRepository;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final TestParticipationRepository testParticipationRepository;

    private final ChildRepository childRepository;

    // 자녀 성향 질문 조회
    @Override
    public TestQuestionResponseDto getTestQuestion(Long testId, Pageable pageable) {

        Test test = testRepository.findById(testId).orElseThrow(() -> new IllegalArgumentException("테스트 정보를 찾을 수 없습니다."));

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTest(test, pageable);

        List<Map<Long, String>> questionList = new ArrayList<>();

        for (TestQuestion testQuestion : testQuestionList) {
            Map<Long, String> questionMap = new HashMap<>();
            questionMap.put(testQuestion.getQuestionId(), testQuestion.getQuestionText());
            questionList.add(questionMap);
        }

        return new TestQuestionResponseDto(test.getName(), test.getDescription(), questionList);
    }

    // 자녀 성향 진단 참여 등록
    @Override
    @Transactional
    public void insertTestParticipation(TestParticipationRequestDto dto) {

        Test test = testRepository.findById(dto.getTestId())
                .orElseThrow(() -> new IllegalArgumentException("테스트 정보를 찾을 수 없습니다."));

        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("아이 정보를 찾을 수 없습니다."));

        TestParticipation testParticipation = dto.toEntity(test, child);

        testParticipationRepository.save(testParticipation);
    }
}

