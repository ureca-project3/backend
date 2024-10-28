package com.triple.backend.batch.tasklet.syncFeedbackStep;

import com.triple.backend.batch.dto.FeedbackDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MySqlFeedbackWriter implements ItemWriter<FeedbackDto> {
    /*
    FeedbackDto 객체들을 MySQL의 feedback 테이블에 삽입한다
    NamedParameterJdbcTemplate는 SQL 쿼리를 실행하기 위한 템플릿이다
     */
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void write(Chunk<? extends FeedbackDto> chunk) throws Exception {
        String insertQuery = """
            INSERT INTO feedback (child_id, book_id, like_status, hate_status, created_at)
            VALUES (:childId, :bookId, :likeStatus, :hateStatus, current_timestamp)
            ON DUPLICATE KEY UPDATE
                like_status = VALUES(like_status),
                hate_status = VALUES(hate_status)
            """;

        List<? extends FeedbackDto> items = chunk.getItems();
        List<FeedbackDto> updates = new ArrayList<>(items);

        // 각 FeedbackDto 객체는 MapSqlParameterSource를 통해 sql문에 매핑된다
        // 이후 FeedbackDto 객체들이 배열 형태의 SqlParameterSource[]로 전달되어 chunk 크기만큼 한꺼번에 쿼리가 날아간다
        SqlParameterSource[] batchParams = updates.stream()
                .map(dto -> new MapSqlParameterSource()
                        .addValue("childId", dto.getChildId())
                        .addValue("bookId", dto.getBookId())
                        .addValue("likeStatus", dto.isLikeStatus())
                        .addValue("hateStatus", dto.isHateStatus()))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(insertQuery, batchParams);
    }
}
