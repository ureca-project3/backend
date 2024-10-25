package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.entity.TestParticipation;
import com.triple.backend.test.repository.TestAnswerRepository;
import com.triple.backend.test.repository.TestParticipationRepository;
import com.triple.backend.test.repository.TestQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MbtiHistoryServiceImpl implements MbtiHistoryService {

    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final ChildTraitsRepository childTraitsRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final TestParticipationRepository testParticipationRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final ChildRepository childRepository;

    // 자녀 성향 히스토리 논리적 삭제
    @Override
    @Transactional
    public MbtiHistoryDeletedResponseDto deleteMyChildTraitHistory(Long historyId, Long childId) {

        Long historyCount = mbtiHistoryRepository.count();

        Child child = childRepository.findById(childId).orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        if(historyCount == 1){
            MbtiHistory mbtiHistory = mbtiHistoryRepository.save(MbtiHistory.builder()
                    .child(child)
                    .currentMbti("INFP")
                    .reason("010")
                    .isDeleted(false)
                    .build());
        }

        MbtiHistory mbtiHistory  = mbtiHistoryRepository.findById(historyId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀 성향 진단 결과"));

        mbtiHistory.updateDeleted(
                true
        );

        MbtiHistory deleteMbtiHistoryResult = mbtiHistoryRepository.save(mbtiHistory);

        return new MbtiHistoryDeletedResponseDto(deleteMbtiHistoryResult.isDeleted());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpOldRecords() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);

        List<MbtiHistory> mbtiHistoryList = mbtiHistoryRepository.findByReasonAndIsDeleted("010", true);

        // mbtiHistory 목록 중 30일 이상 경과한 것만 필터링하기
        List<MbtiHistory> deleteMbtiHistoryList = mbtiHistoryList.stream()
                .filter(mbtiHistory -> mbtiHistory.getModifiedAt().isBefore(thresholdDate))
                .collect(Collectors.toList());

        // 자녀 성향 먼저 삭제!!
        childTraitsRepository.deleteAllInBatch(
                childTraitsRepository.findByMbtiHistoryIn(deleteMbtiHistoryList)
        );

        // 테스트 참여 찾고
        List<TestParticipation> testParticipationList = testParticipationRepository.findAllById(
                deleteMbtiHistoryList.stream()
                        .map(MbtiHistory::getReasonId)
                        .collect(Collectors.toList())
        );

        // 테스트 답변 삭제 후
        testAnswerRepository.deleteAllInBatch(
                testAnswerRepository.findByTestAnswerIdTestParticipationIn(testParticipationList)
        );

        // 테스트 참여 삭제
        testParticipationRepository.deleteAllInBatch(testParticipationList);

        // 마지막으로 MBTI 히스토리 삭제
        mbtiHistoryRepository.deleteAllInBatch(deleteMbtiHistoryList);
    }
}
