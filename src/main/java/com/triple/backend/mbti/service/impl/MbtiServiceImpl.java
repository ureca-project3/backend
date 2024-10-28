package com.triple.backend.mbti.service.impl;

import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.mbti.service.MbtiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MbtiServiceImpl implements MbtiService {

    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final ChildTraitsRepository childTraitsRepository;

    @Override
    public void insertNewChildTraits() {
        List<MbtiHistory> mbtiHistoryList = mbtiHistoryRepository.findNotHavingChildTraits();
        if (mbtiHistoryList != null && !mbtiHistoryList.isEmpty()) {
            for (MbtiHistory mbtiHistory : mbtiHistoryList) {
                List<ChildTraits> childTraitsList = childTraitsRepository.findLatestTraitsByHistoryId(mbtiHistory.getReasonId());
                if (childTraitsList != null && !childTraitsList.isEmpty()) {
                    for(ChildTraits childTraits : childTraitsList) {
                        ChildTraits NewChildTraits = ChildTraits.builder()
                                .mbtiHistory(mbtiHistory)
                                .trait(childTraits.getTrait())
                                .traitScore(childTraits.getTraitScore())
                                .createdAt(LocalDateTime.now())
                                .build();
                        childTraitsRepository.save(NewChildTraits);
                    }
                } else {
                    throw NotFoundException.entityNotFound("이전 MBTI 성향");
                }
            }
        }
    }
}
