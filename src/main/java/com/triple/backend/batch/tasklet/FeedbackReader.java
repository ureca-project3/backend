package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.FeedbackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class FeedbackReader implements ItemReader<List<FeedbackDto>> {

    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    private final HashOperations<String, String, Set<Long>> hashOperations;
    private Iterator<Map.Entry<String, Set<Long>>> likeIterator;
    private Iterator<Map.Entry<String, Set<Long>>> hateIterator;

    /*
    확인할 사항 : Chunk 100개씩 제대로 처리가 되고 있는 걸까?
     */
    @Override
    public List<FeedbackDto> read() throws Exception {
        List<FeedbackDto> feedbackDtoList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            // Like 데이터 읽기
            if (likeIterator == null || !likeIterator.hasNext()) {
                likeIterator = hashOperations.entries(LIKE_HASH_KEY).entrySet().iterator();
            }
            if (likeIterator.hasNext()) {
                Map.Entry<String, Set<Long>> entry = likeIterator.next();
                Long childId = Long.valueOf(entry.getKey());
                Set<Long> bookIds = entry.getValue();

                for (Long bookId : bookIds) {
                    FeedbackDto feedbackDto = new FeedbackDto(childId, bookId, true, false);
                    feedbackDtoList.add(feedbackDto);
                }
            }

            // Hate 데이터 읽기
            if (hateIterator == null || !hateIterator.hasNext()) {
                hateIterator = hashOperations.entries(HATE_HASH_KEY).entrySet().iterator();
            }

            if (hateIterator.hasNext()) {
                Map.Entry<String, Set<Long>> entry = likeIterator.next();
                Long childId = Long.valueOf(entry.getKey());
                Set<Long> bookIds = entry.getValue();

                for (Long bookId : bookIds) {
                    FeedbackDto feedbackDto = new FeedbackDto(childId, bookId, false, true);
                    feedbackDtoList.add(feedbackDto);
                }
            }

            if (!likeIterator.hasNext() && !hateIterator.hasNext()) {
                break;
            }
        }

        return feedbackDtoList.isEmpty() ? null : feedbackDtoList;
    }
}
