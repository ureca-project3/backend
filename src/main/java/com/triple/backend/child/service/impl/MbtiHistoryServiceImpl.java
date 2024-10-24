package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.entity.TestAnswerId;
import com.triple.backend.test.entity.TestParticipation;
import com.triple.backend.test.entity.TestQuestion;
import com.triple.backend.test.repository.TestAnswerRepository;
import com.triple.backend.test.repository.TestParticipationRepository;
import com.triple.backend.test.repository.TestQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MbtiHistoryServiceImpl implements MbtiHistoryService {

    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final ChildTraitsRepository childTraitsRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final TestParticipationRepository testParticipationRepository;
    private final TestQuestionRepository testQuestionRepository;

    // 자녀 성향 히스토리 논리적 삭제
    @Override
    @Transactional
    public MbtiHistoryDeletedResponseDto deleteMyChildTraitHistory(Long historyId) {
        MbtiHistory mbtiHistory  = mbtiHistoryRepository.findById(historyId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀 성향 진단 결과"));

        mbtiHistory.updateDeleted(
                true
        );

        MbtiHistory deleteMbtiHistoryResult = mbtiHistoryRepository.save(mbtiHistory);

        return new MbtiHistoryDeletedResponseDto(deleteMbtiHistoryResult.isDeleted());
    }

    // 자녀 성향 히스토리 물리적 삭제
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpOldRecords() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);

        List<MbtiHistory> mbtiHistoryList = mbtiHistoryRepository.findByReasonAndIsDeleted("010", true);

        for (MbtiHistory mbtiHistory : mbtiHistoryList) {

            // mbtiHistory 수정일자 30일 이상 경과 확인
            if (mbtiHistory.getModifiedAt().isBefore(thresholdDate)) { // 30일 이상 경과 시 테스트 참여 조회하여 히스토리, 자녀 성향, 테스트 참여, 테스트 답변 삭제
                TestParticipation testParticipation = testParticipationRepository.findById(mbtiHistory.getReasonId())
                        .orElseThrow(() -> NotFoundException.entityNotFound("테스트 참여 기록"));

                childTraitsRepository.deleteByMbtiHistory(mbtiHistory);
                mbtiHistoryRepository.delete(mbtiHistory);

                // 질문 갯수대로 반복하여 테스트 답변 삭제
                List<TestQuestion> testQuestionList = testQuestionRepository.findByTest(testParticipation.getTest());

                for (TestQuestion testQuestion : testQuestionList) {
                    TestAnswerId testAnswerId = new TestAnswerId(testParticipation, testQuestion);
                    testAnswerRepository.deleteByTestAnswerId(testAnswerId);
                }

                testParticipationRepository.delete(testParticipation);
            }

        }

        // 30일 이상 미경과 시 넘어가기

    }
}
