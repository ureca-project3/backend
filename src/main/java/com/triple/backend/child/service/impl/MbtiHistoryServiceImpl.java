package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        MbtiHistory deleteMbtiHistory = new MbtiHistory(mbtiHistory.getHistoryId(), mbtiHistory.getChild(),
                mbtiHistory.getCurrentMbti(), mbtiHistory.getCreatedAt(), mbtiHistory.getReason(), true);

        MbtiHistory deleteMbtiHistoryResult = mbtiHistoryRepository.save(deleteMbtiHistory);

        return new MbtiHistoryDeletedResponseDto(deleteMbtiHistoryResult.isDeleted());
    }
}
