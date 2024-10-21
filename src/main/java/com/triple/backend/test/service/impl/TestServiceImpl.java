package com.triple.backend.test.service.impl;

import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.dto.*;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestParticipation;
import com.triple.backend.test.entity.TestQuestion;
import com.triple.backend.test.repository.TestParticipationRepository;
import com.triple.backend.test.repository.TestQuestionRepository;
import com.triple.backend.test.repository.TestRepository;
import com.triple.backend.test.entity.*;
import com.triple.backend.test.repository.*;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final ChildTraitsRepository childTraitsRepository;
    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final TestParticipationRepository testParticipationRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final ChildRepository childRepository;

    private final TraitRepository traitRepository;

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
    public TestResultRequestDto getTestResult(Long childId) {

        MbtiHistory history = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);
        Long historyId = history.getHistoryId();

        TestParticipation testParticipation = testParticipationRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);
        Long testId = testParticipation.getTest().getTestId();

        List<TraitDataResponseDto> traitDataDtoList = childTraitsRepository.findTraitsByChildAndTest(childId, historyId, testId);

        return new TestResultRequestDto(traitDataDtoList);
    }

    // 자녀 성향 진단 결과 등록
    @Override
    @Transactional
    public void insertTestResult(Long testId, TestAnswerRequestDto testAnswerRequestDto, Long childId) {

        // Token 자녀ID
        Child child = childRepository.findById(childId).orElseThrow( () -> NotFoundException.entityNotFound("자녀"));

        // 테스트 참여ID 조회
        Test test = testRepository.findById(testId).orElseThrow( () -> NotFoundException.entityNotFound("테스트"));
        TestParticipation testParticipation = testParticipationRepository.findTopByChildAndTestOrderByCreatedAtDesc(child, test);

        // MBTI 성향 조회
        List<Trait> traitList = traitRepository.findByTest(test);

        // 성향 점수 변수
        List<Map<String, Integer>> totalTraitCount = new ArrayList<>();
        for (Trait trait : traitList) {
            Map<String, Integer> traitCountMap = new HashMap<>();
            traitCountMap.put(trait.getTraitName(), (trait.getMaxScore() + trait.getMinScore())/2);
            totalTraitCount.add(traitCountMap);
        }

        // 테스트 답변 등록
        for( Map<Long, Integer> answer : testAnswerRequestDto.getAnswerList() ) {

            for (Map.Entry<Long, Integer> entry : answer.entrySet()) {

                // 자녀 성향 테스트 응답 등록
                TestQuestionTraitResponseDto testQuestionTraitResponseDto = testQuestionRepository.findQuestionWithTraitById(entry.getKey());
                TestQuestion testQuestion = TestQuestion.builder()
                        .questionId(testQuestionTraitResponseDto.getQuestionId())
                        .test(testQuestionTraitResponseDto.getTest())
                        .trait(testQuestionTraitResponseDto.getTrait())
                        .questionText(testQuestionTraitResponseDto.getQuestionText()).build();
                TestAnswerId testAnswerId = new TestAnswerId(testParticipation, testQuestion);
                testAnswerRepository.save(new TestAnswer(testAnswerId, entry.getValue()));

                // 성향 점수 증감
                for (Map<String, Integer> traitCount : totalTraitCount) {
                    for (Map.Entry<String, Integer> traitCountEntry : traitCount.entrySet()) {
                        if(traitCountEntry.getKey().equals(testQuestionTraitResponseDto.getTraitName())) {
                            traitCountEntry.setValue(traitCountEntry.getValue() + entry.getValue());
                        }
                    }
                }

            }
        }

        // 현재 성향 MBTI
        String currentMbti = "";

        for (Map<String, Integer> traitCount : totalTraitCount) {
            // 최종 성향 점수
            for (String key : traitCount.keySet()) {
                // 현재 성향 점수 확인 TEST
                Integer value = traitCount.get(key);

                // 현재 성향 MBTI 문자열 구하기
                TraitType traitType = TraitType.valueOf(key);
                currentMbti += traitType.getTraitByScore(value);
            }
        }

        // MBTI 히스토리 등록
        MbtiHistory mbtiHistory = mbtiHistoryRepository.save(MbtiHistory.builder()
                .child(child)
                .currentMbti(currentMbti)
                .createdAt(LocalDateTime.now())
                .reason("자녀 성향 진단")
                .isDeleted(false)
                .build());

        // 자녀 성향 등록
        for (Map<String, Integer> traitCount : totalTraitCount) {
            for (String key : traitCount.keySet()) {
                Integer value = traitCount.get(key);
                for( Trait trait : traitList) {
                    if(key.equals(trait.getTraitName())) {
                        childTraitsRepository.save(ChildTraits.builder()
                                .mbtiHistory(mbtiHistory)
                                .trait(trait)
                                .traitScore(value)
                                .createdAt(LocalDateTime.now())
                                .build());
                    }
                }
            }
        }

    }

    // 자녀 성향 진단 참여 등록
    @Override
    @Transactional
    public void insertTestParticipation(TestParticipationRequestDto dto) {

        Test test = testRepository.findById(dto.getTestId())
                .orElseThrow(() -> NotFoundException.entityNotFound("테스트"));

        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        TestParticipation testParticipation = dto.toEntity(test, child);

        testParticipationRepository.save(testParticipation);
    }

}
