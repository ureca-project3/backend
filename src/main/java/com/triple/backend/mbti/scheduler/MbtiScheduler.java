package com.triple.backend.mbti.scheduler;

import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j(topic = "Batch Scheduler")
@Configuration
@RequiredArgsConstructor
public class MbtiScheduler {

    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final ChildTraitsRepository childTraitsRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void insertNewChildTraits() {
        List<MbtiHistory> mbtiHistoryList = mbtiHistoryRepository.findNotHavingChildTraits();
        if (mbtiHistoryList != null && !mbtiHistoryList.isEmpty()) {
            for (MbtiHistory mbtiHistory : mbtiHistoryList) {
                List<ChildTraits> childTraitsList = childTraitsRepository.findLatestTraitsByHistoryId(mbtiHistory.getReasonId());
                for(ChildTraits childTraits : childTraitsList) {
                    ChildTraits NewChildTraits = ChildTraits.builder()
                            .mbtiHistory(mbtiHistory)
                            .trait(childTraits.getTrait())
                            .traitScore(childTraits.getTraitScore())
                            .createdAt(LocalDateTime.now())
                            .build();
                    childTraitsRepository.save(NewChildTraits);
                }
            }
        }
    }
}
