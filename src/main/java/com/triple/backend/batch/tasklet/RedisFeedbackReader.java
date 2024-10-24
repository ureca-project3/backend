package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.FeedbackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisFeedbackReader implements ItemReader<FeedbackDto> {

    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    private final HashOperations<String, String, Set<Long>> hashOperations;

    private Iterator<Map.Entry<String, Set<Long>>> likeIterator;
    private Iterator<Long> likeBookIterator;

    private Iterator<Map.Entry<String, Set<Long>>> hateIterator;
    private Iterator<Long> hateBookIterator;

    @Override
    public FeedbackDto read() throws Exception{
        Long childId = null;

        if (likeIterator == null || (likeBookIterator != null && !likeBookIterator.hasNext())) {
            likeIterator = hashOperations.entries(LIKE_HASH_KEY).entrySet().iterator();
        }

        if (likeBookIterator == null || !likeBookIterator.hasNext()) {
            if (likeIterator.hasNext()) {
                Map.Entry<String, Set<Long>> entry = likeIterator.next();
                childId = Long.valueOf(entry.getKey());
                likeBookIterator = entry.getValue().iterator();
            }
        }

        if (likeBookIterator != null && likeBookIterator.hasNext()) {
            Long bookId = likeBookIterator.next();
            return new FeedbackDto(childId, bookId, true, false);
        }

        // Hate 데이터 읽기
        if (hateIterator == null || (hateBookIterator != null && !hateBookIterator.hasNext())) {
            hateIterator = hashOperations.entries(HATE_HASH_KEY).entrySet().iterator();
        }

        if (hateBookIterator == null || !hateBookIterator.hasNext()) {
            if (hateIterator.hasNext()) {
                Map.Entry<String, Set<Long>> entry = hateIterator.next();
                childId = Long.valueOf(entry.getKey());
                hateBookIterator = entry.getValue().iterator();
            }
        }

        if (hateBookIterator != null && hateBookIterator.hasNext()) {
            Long bookId = likeBookIterator.next();
            return new FeedbackDto(childId, bookId, false, true);
        }

        return null;
    }
}
