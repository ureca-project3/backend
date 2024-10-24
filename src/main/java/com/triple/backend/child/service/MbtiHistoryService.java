package com.triple.backend.child.service;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;

public interface MbtiHistoryService {

    // 자녀 성향 히스토리 논리적 삭제
    MbtiHistoryDeletedResponseDto deleteMyChildTraitHistory(Long historyId, Long childId);

    // 물리적 삭제 (자녀 성향, 히스토리)
    void cleanUpOldRecords();
}
