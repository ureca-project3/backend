package com.triple.backend.batch.mapper;

import com.triple.backend.batch.dto.BookTraitsDto;
import com.triple.backend.batch.dto.ChildTraitsDto;
import com.triple.backend.batch.dto.FeedbackAndTraitsDto;
import com.triple.backend.batch.dto.TraitsChangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class FeedbackAndTraitsRowMapper implements RowMapper<FeedbackAndTraitsDto> {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public FeedbackAndTraitsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeedbackAndTraitsDto dto = new FeedbackAndTraitsDto();
        dto.setChildId(rs.getLong("child_id"));
        dto.setBookId(rs.getLong("book_id"));
        dto.setLikeStatus(rs.getBoolean("like_status"));
        dto.setHateStatus(rs.getBoolean("hate_status"));

        // 추가 데이터 조회
        dto.setChildTraits(fetchChildTraits(dto.getChildId()));
        dto.setBookTraits(fetchBookTraits(dto.getBookId()));
        dto.setTraitsChanges(fetchTraitsChange(dto.getChildId()));

        return dto;
    }

    private List<ChildTraitsDto> fetchChildTraits(Long childId) {
        String sql = """
                
                                SELECT child_traits_id, trait_score, trait_id
                FROM (
                    SELECT
                        ct.child_traits_id,
                        ct.trait_score,
                        ct.trait_id,
                        ROW_NUMBER() OVER (PARTITION BY ct.trait_id ORDER BY ct.created_at DESC) AS rn
                    FROM child_traits ct
                    WHERE ct.history_id = (
                        SELECT history_id
                        FROM mbti_history
                        WHERE child_id = :childId
                        ORDER BY created_at DESC
                        LIMIT 1
                    )
                ) AS ranked_traits
                WHERE rn = 1
                ORDER BY trait_id ASC;
                """;

        return namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("childId", childId),
                new ChildTraitsRowMapper()
        );
    }

    private List<BookTraitsDto> fetchBookTraits(Long bookId) {
        String sql = """
            SELECT bt.book_traits_id, bt.trait_score, bt.trait_id
            FROM book_traits bt
            WHERE bt.book_id = :bookId
            ORDER BY bt.trait_id ASC
            LIMIT 4
            """;
        return namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("bookId", bookId),
                new BookTraitsRowMapper()
        );
    }

    private List<TraitsChangeDto> fetchTraitsChange(Long childId) {
        String sql = """
            SELECT tc.trait_change_id, tc.change_amount, tc.child_id, tc.trait_id
            FROM traits_change tc
            WHERE tc.child_id = :childId
            ORDER BY tc.trait_id ASC
            LIMIT 4
            """;
        return namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("childId", childId),
                new TraitsChangeRowMapper()
        );
    }
}