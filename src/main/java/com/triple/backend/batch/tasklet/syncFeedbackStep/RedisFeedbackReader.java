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
    /*
    Redis에서 데이터를 하나씩 읽어와, childId, bookId, likeStatus, hateStatus로 구성된 FeedbackDto를 만들어 반환한다.
    각각의 HASH_KEY는 redis에서 좋아요/싫어요를 가져오기 위한 키 값이다.
     */
    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    private final HashOperations<String, String, Set<Long>> hashOperations;

    /*
    isProcessingLikes: 현재 처리하는 데이터가 좋아요인지 싫어요인지 구분하기 위해 사용한다.
    currentIterator: 전체 redis를 순회하기 위한 iterator이다.
    currentChildId: 현재 처리 중인 childId를 저장한다.
    currentBookIterator: 자녀별 좋아요/싫어요 bookId를 순회하기 위한 iterator이다.
     */
    private boolean isProcessingLikes = true;
    private Iterator<Map.Entry<String, Set<Long>>> currentIterator;
    private String currentChildId;
    private Iterator<Long> currentBookIterator;

    @Override
    public FeedbackDto read() {
        System.out.println("RedisFeedbackReader read() 호출");

        // currentIterator가 null일 경우 initializeIterator()를 호출하여 데이터를 초기화한다
        if (currentIterator == null) {
            log.info("Iterator 초기화");
            initializeIterator();
        }

        while (true) {
            // 자녀가 좋아요/싫어요한 책이 남아 있는 경우 데이터를 들고와 FeedbackDto를 변환 후 반환한다
            if (currentBookIterator != null && currentBookIterator.hasNext()) {
                Long bookId = currentBookIterator.next();
                log.debug("현재 BookId 처리: {}", bookId);
                return new FeedbackDto(Long.valueOf(currentChildId), bookId, isProcessingLikes, !isProcessingLikes);
            }

            // 다음 자녀가 있는 경우, 다음 자녀의 childId와 Set<Long> bookIds를 들고와 초기화한다
            if (currentIterator.hasNext()) {
                Map.Entry<String, Set<Long>> entry = currentIterator.next();
                currentChildId = entry.getKey();
                currentBookIterator = entry.getValue().iterator();
                log.debug("다음 Child로 이동: {}", currentChildId);
                continue;
            }

            // 좋아요를 모두 처리했다면, isProcessingLikes를 false로 만들고 initializeIterator()를 호출하여 싫어요 데이터를 초기화한다
            if (isProcessingLikes) {
                log.debug("Likes 처리 완료, Hates로 전환");
                isProcessingLikes = false;
                initializeIterator();
                continue;
            }

            // 모든 데이터를 다 읽으면 null을 반환하여 데이터 처리가 완료되었음을 알린다
            // 다음 배치 작업을 위해 isProcessingLikes를 true로 설정해 다시 좋아요 데이터부터 읽도록 하고, 모든 Iterator와 자녀 ID도 초기화한다
            log.debug("모든 데이터 처리 완료");
            reset();
            return null;
        }
    }

    /*
    initializeIterator(): iterator를 초기화하는 private 메서드
    좋아요/싫어에 해당하는 데이터를 Redis에서 불러와 currentIterator에 할당한다
     */
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

    private void reset() {
        isProcessingLikes = true;
        currentIterator = null;
        currentBookIterator = null;
        currentChildId = null;
        log.debug("RedisFeedbackReader 리셋 완료");
    }
}
