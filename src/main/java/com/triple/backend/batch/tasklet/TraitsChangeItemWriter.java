package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.UpdateTraitChangeDto;
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
public class TraitsChangeItemWriter implements ItemWriter<UpdateTraitChangeDto> {

    @Qualifier("mainNamedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void write(Chunk<? extends UpdateTraitChangeDto> chunk) throws Exception {
        String updateQuery = """
            UPDATE traits_change
            SET change_amount = :changeAmount
            WHERE child_id = :childId
            AND trait_id = :traitId
            """;

        List<? extends UpdateTraitChangeDto> items = chunk.getItems();
        List<UpdateTraitChangeDto> updates = new ArrayList<>(items);

        SqlParameterSource[] batchParams = updates.stream()
                .map(dto -> new MapSqlParameterSource()
                        .addValue("childId", dto.getChildId())
                        .addValue("traitId", dto.getTraitId())
                        .addValue("changeAmount", dto.getChangeAmount()))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(updateQuery, batchParams);
    }
}

