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
    private final MbtiRepository mbtiRepository;
    private final TraitRepository traitRepository;

    // 자녀 성향 질문 조회
    @Override
    public TestQuestionResponseDto getTestQuestion(Long testId, Pageable pageable) {

        Test test = testRepository.findById(testId).orElseThrow( () -> NotFoundException.entityNotFound("테스트"));

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTest(test, pageable);

        Map<Long, String> questionList = new LinkedHashMap<>();

        for (TestQuestion testQuestion : testQuestionList) {
            questionList.put(testQuestion.getQuestionId(), testQuestion.getQuestionText());
        }

        return new TestQuestionResponseDto(test.getName(), test.getDescription(), questionList);
    }


    // 자녀 성향 진단 결과 조희
    @Override
    public TestResultResponseDto getTestResult(Long childId) {

        MbtiHistory history = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("최신 히스토리"));

        Long historyId = history.getHistoryId();

        TestParticipation testParticipation = testParticipationRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);
        Long testId = testParticipation.getTest().getTestId();

        List<TraitDataResponseDto> traitDataDtoList = childTraitsRepository.findTraitsByChildAndTest(childId, historyId, testId);

        Mbti mbti = mbtiRepository.findByName(MbtiType.valueOf(history.getCurrentMbti()))
                .orElseThrow(() -> NotFoundException.entityNotFound("MBTI"));

        return new TestResultResponseDto(
                traitDataDtoList,
                mbti.getName(),
                mbti.getDescription(),
                mbti.getImage(),
                mbti.getPhrase()
        );
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


        Map<String, Integer> totalTraitCount = new LinkedHashMap<>();
        for (Trait trait : traitList) {
            totalTraitCount.put(trait.getTraitName(), (trait.getMaxScore() + trait.getMinScore()) / 2);
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


                // 성향 점수 업데이트
                String traitName = testQuestionTraitResponseDto.getTraitName();
                totalTraitCount.put(traitName, totalTraitCount.get(traitName) + entry.getValue());

            }
        }


        // 현재 성향 MBTI 계산
        StringBuilder currentMbti = new StringBuilder();
        for (Map.Entry<String, Integer> traitCountEntry : totalTraitCount.entrySet()) {
            String traitName = traitCountEntry.getKey();
            Integer score = traitCountEntry.getValue();
            TraitType traitType = TraitType.valueOf(traitName);
            currentMbti.append(traitType.getTraitByScore(score));
        }

        // MBTI 히스토리 등록
        MbtiHistory mbtiHistory = mbtiHistoryRepository.save(MbtiHistory.builder()
                .child(child)
                .currentMbti(currentMbti.toString())
                .reason("020")
                .reasonId(testParticipation.getTestParticipationId())
                .isDeleted(false)
                .build());


        // 자녀 성향 등록
        for (Map.Entry<String, Integer> traitCountEntry : totalTraitCount.entrySet()) {
            String traitName = traitCountEntry.getKey();
            Integer score = traitCountEntry.getValue();

            for (Trait trait : traitList) {
                if (trait.getTraitName().equals(traitName)) {
                    childTraitsRepository.save(ChildTraits.builder()
                            .mbtiHistory(mbtiHistory)
                            .trait(trait)
                            .traitScore(score)
                            .createdAt(LocalDateTime.now())
                            .build());
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