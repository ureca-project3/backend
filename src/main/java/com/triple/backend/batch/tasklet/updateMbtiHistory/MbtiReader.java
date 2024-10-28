package com.triple.backend.batch.tasklet.updateMbtiHistory;

import com.triple.backend.batch.dto.MbtiWithTraitScoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MbtiReader implements ItemReader<List<MbtiWithTraitScoreDto>> {

    /*
    groupedData : MbtiWithTraitScoreDto 데이터가 childId별로 그룹화되어 저장되는 리스트. child당 크기 4인 리스트가 저장되어 있다
    nextIndex : groupedData가 다음으로 반환할 인덱스
     */
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private List<List<MbtiWithTraitScoreDto>> groupedData = null;
    private int nextIndex = 0;

    @Override
    public List<MbtiWithTraitScoreDto> read() {
        /*
        groupedData가 null일 경우 fetchGroupedData() 메서드를 호출해 데이터를 초기화한다
        nextIndex가 groupedData를 아직 다 가져오지 못했다면 해당 인덱스의 리스트를 반환하고, nextIndex를 증가시킨다
        nextIndex가 groupedData를 다 가져왔다면 모든 데이터 처리가 완료된 것으로 간주하고 null을 반환한다
         */
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
        /*
        오늘 날짜에 새롭게 추가된 child_traits 데이터를 가져오며, childId별로 trait_score의 최신 상태를 조회하는 쿼리
        Step1 : 오늘 생성된 child_traits에서 history_id만 그룹화하여 recent_history 뷰를 만든다
        Step2 : child_traits 테이블을 recent_history와 inner join하여 오늘 날짜에 추가된 history_id를 가진 레코드만 선택한다
        Step3 : 각 history_id, trait_id 조합에서 가장 최근(MAX(created_at))의 trait 기록을 선택하여 latest 뷰를 만든다
        Step4 : child_traits와 latest 뷰를 inner join하여 최신 trait 데이터를 가져온다
        Step5 : mbti_history 테이블을 left join하여 history_id를 기준으로 각 자녀의 MBTI 정보를 결합한다 (ex.ESTP)
                즉, 각 childId별로 trait_score의 최신 상태와 MBTI(current_mbti) 정보를 함께 가져온다
        Step6 : child_id와 trait_id별로 정렬한다
         */
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

