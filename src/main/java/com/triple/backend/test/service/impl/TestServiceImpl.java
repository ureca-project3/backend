package com.triple.backend.test.service.impl;

import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.test.dto.TestAnswerRequestDto;
import com.triple.backend.test.dto.TestQuestionResponseDto;
import com.triple.backend.test.dto.TestQuestionTraitResponseDto;
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

    private final TestAnswerRepository testAnswerRepository;

    private  final TestParticipationRepository testParticipationRepository;

    private final MbtiHistoryRepository mbtiHistoryRepository;

    private final ChildRepository childRepository;

    private final ChildTraitsRepository childTraitsRepository;

    private final TraitRepository traitRepository;

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

    // 자녀 성향 진단 결과 등록
    @Override
    @Transactional
    public void insertTestResult(Long testId, TestAnswerRequestDto testAnswerRequestDto) {

        // Token 자녀ID
        /*
        2024.10.20
        자녀ID Token으로 수정 필요
         */
        Long childId = 1L;
        Child child = childRepository.findById(childId).orElseThrow( () -> new IllegalArgumentException("로그인이 되어 있는지 확인 부탁드립니다."));

        // 테스트 참여ID 조회
        Test test = testRepository.findById(testId).orElseThrow( () -> new IllegalArgumentException("테스트 정보를 찾을 수 없습니다."));
        TestParticipation testParticipation = testParticipationRepository.findByChildAndTest(child, test);

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
                TestQuestion testQuestion = new TestQuestion(testQuestionTraitResponseDto.getQuestionId(), testQuestionTraitResponseDto.getTest(),
                        testQuestionTraitResponseDto.getTrait(), testQuestionTraitResponseDto.getQuestionText());
                TestAnswerPK testAnswerPK = new TestAnswerPK(testParticipation, testQuestion);
                System.out.println("testParticipationId : " + testParticipation.getTestParticipationId());
                System.out.println("questionId " + testQuestion.getQuestionId() + " , test : " + testQuestion.getTest().getTestId() + " , test : " + testQuestion.getTrait().getTraitId());
                System.out.println("entry.getValue() : " + entry.getValue());
                testAnswerRepository.save(new TestAnswer(testAnswerPK, entry.getValue()));

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
                System.out.println(key + " : " + value);

                // 현재 성향 MBTI 문자열 구하기
                /*
                    2024.10.20
                    erd에 MBTI 알파벳이 저장되어 있지 않아 DB에서 가져와서 등록 불가능
                 */
                if( value > 50) {
                    switch (key) {
                        case "에너지방향" : currentMbti += 'E'; break;
                        case "인식기능" : currentMbti += 'S'; break;
                        case "판단기능" : currentMbti += 'T'; break;
                        case "생활양식" : currentMbti += 'J'; break;
                    }
                } else {
                    switch (key) {
                        case "에너지방향" : currentMbti += 'I'; break;
                        case "인식기능" : currentMbti += 'N'; break;
                        case "판단기능" : currentMbti += 'F'; break;
                        case "생활양식" : currentMbti += 'P'; break;
                    }
                }
            }
        }

        // MBTI 히스토리 등록
        MbtiHistory mbtiHistory = mbtiHistoryRepository.save(new MbtiHistory(child, currentMbti, LocalDateTime.now(), "자녀 성향 진단", false));

        // 자녀 성향 등록
        for (Map<String, Integer> traitCount : totalTraitCount) {
            for (String key : traitCount.keySet()) {
                Integer value = traitCount.get(key);
                for( Trait trait : traitList) {
                    if(key.equals(trait.getTraitName())) {
                        childTraitsRepository.save(new ChildTraits(mbtiHistory, trait, value, LocalDateTime.now()));
                    }
                }
            }
        }

    }

}
