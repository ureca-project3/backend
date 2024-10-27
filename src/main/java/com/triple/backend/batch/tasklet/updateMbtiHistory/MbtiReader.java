package com.triple.backend.batch.tasklet.updateMbtiHistory;

import com.triple.backend.batch.dto.MbtiWithTraitScoreDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MbtiReader implements ItemReader<List<MbtiWithTraitScoreDto>> {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private List<List<MbtiWithTraitScoreDto>> groupedData = null;
    private int nextIndex = 0;

    public MbtiReader(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<MbtiWithTraitScoreDto> read() {
        if (groupedData == null) {
            groupedData = fetchGroupedData();
        }

        if (nextIndex < groupedData.size()) {
            return groupedData.get(nextIndex++);
        } else {
            return null;
        }
    }

    private List<List<MbtiWithTraitScoreDto>> fetchGroupedData() {
        String sql = """
            SELECT 
                mh.child_id, ct.trait_id, ct.trait_score, ct.created_at, 
                mh.current_mbti, mh.history_id
            FROM child_traits ct
            INNER JOIN (
                SELECT history_id
                FROM child_traits
                WHERE DATE(created_at) = CURDATE()
                GROUP BY history_id
            ) recent_history ON ct.history_id = recent_history.history_id
            INNER JOIN (
                SELECT history_id, trait_id, MAX(created_at) AS max_created_at
                FROM child_traits
                GROUP BY history_id, trait_id
            ) latest ON ct.history_id = latest.history_id 
                     AND ct.trait_id = latest.trait_id 
                     AND ct.created_at = latest.max_created_at
            LEFT JOIN mbti_history mh ON ct.history_id = mh.history_id
            ORDER BY mh.child_id, ct.trait_id
        """;

        List<MbtiWithTraitScoreDto> allData = namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> {
            MbtiWithTraitScoreDto data = new MbtiWithTraitScoreDto();
            data.setChildId(rs.getLong("child_id"));
            data.setTraitId(rs.getLong("trait_id"));
            data.setTraitScore(rs.getInt("trait_score"));
            data.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            data.setCurrentMbti(rs.getString("current_mbti"));
            data.setHistoryId(rs.getLong("history_id"));
            return data;
        });

        // childId별로 그룹화
        return allData.stream()
                .collect(Collectors.groupingBy(MbtiWithTraitScoreDto::getChildId))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}

