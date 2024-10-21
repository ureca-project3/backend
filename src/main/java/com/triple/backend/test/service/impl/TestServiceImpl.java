package com.triple.backend.test.service.impl;

import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.dto.TestQuestionResponseDto;
import com.triple.backend.test.dto.TestResultDto;
import com.triple.backend.test.dto.TraitDataDto;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestParticipation;
import com.triple.backend.test.entity.TestQuestion;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.test.repository.TestParticipationRepository;
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
    private final ChildTraitsRepository childTraitsRepository;
    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final TestParticipationRepository testParticipationRepository;

    // 자녀 성향 질문 조회
    @Override
    public TestQuestionResponseDto getTestQuestion(Long testId, Pageable pageable) {

        Test test = testRepository.findById(testId).orElseThrow( () -> NotFoundException.entityNotFound("테스트"));

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTest(test, pageable);

        List<Map<Long, String>> questionList = new ArrayList<>();

        for (TestQuestion testQuestion : testQuestionList) {
            Map<Long, String> questionMap = new HashMap<>();
            questionMap.put(testQuestion.getQuestionId(), testQuestion.getQuestionText());
            questionList.add(questionMap);
        }

        return new TestQuestionResponseDto(test.getName(), test.getDescription(), questionList);
    }

    // 자녀 성향 진단 결과 조희
    @Override
    public TestResultDto getTestResult(Long childId) {

        MbtiHistory history = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);
        Long historyId = history.getHistoryId();

        TestParticipation testParticipation = testParticipationRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);
        Long testId = testParticipation.getTest().getTestId();

        List<TraitDataDto> traitDataDtoList = childTraitsRepository.findTraitsByChildAndTest(childId, historyId, testId);

        return new TestResultDto(traitDataDtoList);
    }
}
