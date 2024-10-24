package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.FeedbackDto;
import com.triple.backend.batch.dto.UpdateTraitChangeDto;
import com.triple.backend.feedback.entity.Feedback;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedbackWriter implements ItemWriter<FeedbackDto> {

    @Qualifier("mainNamedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void write(Chunk<? extends FeedbackDto> chunk) throws Exception {
        String insertQuery = """
            INSERT INTO feedback (child_id, book_id, like_status, hate_status)
            VALUES (:childId, :bookId, :likeStatus, :hateStatus)
            ON DUPLICATE KEY UPDATE
                like_status = VALUES(like_status),
                hate_status = VALUES(hate_status)
            """;

        List<? extends FeedbackDto> items = chunk.getItems();
        List<FeedbackDto> updates = new ArrayList<>(items);

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
