package com.triple.backend.batch.tasklet.syncFeedbackStep;

import com.triple.backend.batch.dto.FeedbackDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisFeedbackReader implements ItemReader<FeedbackDto> {

    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    private final HashOperations<String, String, Set<Long>> hashOperations;

    private boolean isProcessingLikes = true;
    private Iterator<Map.Entry<String, Set<Long>>> currentIterator;
    private String currentChildId;
    private Iterator<Long> currentBookIterator;

    @Override
    public FeedbackDto read() {
        System.out.println("RedisFeedbackReader read() 호출");

        if (currentIterator == null) {
            log.info("Iterator 초기화");
            initializeIterator();
        }

        while (true) {
            if (currentBookIterator != null && currentBookIterator.hasNext()) {
                Long bookId = currentBookIterator.next();
                log.debug("현재 BookId 처리: {}", bookId);
                return new FeedbackDto(Long.valueOf(currentChildId), bookId, isProcessingLikes, !isProcessingLikes);
            }

            if (currentIterator.hasNext()) {
                Map.Entry<String, Set<Long>> entry = currentIterator.next();
                currentChildId = entry.getKey();
                currentBookIterator = entry.getValue().iterator();
                log.debug("다음 Child로 이동: {}", currentChildId);
                continue;
            }

            if (isProcessingLikes) {
                log.debug("Likes 처리 완료, Hates로 전환");
                isProcessingLikes = false;
                initializeIterator();
                continue;
            }

            log.debug("모든 데이터 처리 완료");
            return null;
        }
    }

    private void initializeIterator() {
        String key = isProcessingLikes ? LIKE_HASH_KEY : HATE_HASH_KEY;
        Map<String, Set<Long>> entries = hashOperations.entries(key);

        if (entries.isEmpty()) {
            log.warn("No entries found for key: {}", key);
        }

        currentIterator = entries.entrySet().iterator();
        currentBookIterator = null;
        currentChildId = null;
    }

    public void reset() {
        isProcessingLikes = true;
        currentIterator = null;
        currentBookIterator = null;
        currentChildId = null;
        log.debug("RedisFeedbackReader 리셋 완료");
    }
}
