package com.triple.backend.batch.mapper;

import com.triple.backend.batch.dto.ChildTraitsDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChildTraitsRowMapper implements RowMapper<ChildTraitsDto> {
    @Override
    public ChildTraitsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChildTraitsDto traits = new ChildTraitsDto();
        traits.setChildTraitsId(rs.getLong("child_traits_id"));
        traits.setTraitScore(rs.getInt("trait_score"));
        traits.setTraitId(rs.getLong("trait_id"));
        return traits;
    }
}
