package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MbtiHistoryServiceImpl implements MbtiHistoryService {

    private final MbtiHistoryRepository mbtiHistoryRepository;

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

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpOldRecords() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        mbtiHistoryRepository.deleteOldChildTraits(thresholdDate);
        mbtiHistoryRepository.deleteOldMbtiHistories(thresholdDate);
    }
}
