package com.triple.backend.mbti.scheduler;

import com.triple.backend.mbti.service.MbtiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j(topic = "Mbti Scheduler")
@Configuration
@RequiredArgsConstructor
public class MbtiScheduler {
    private final MbtiService mbtiService;

    @Scheduled(cron = "0 0 3 * * *")
    public void insertNewChildTraits() {
        mbtiService.insertNewChildTraits();
    }
}
