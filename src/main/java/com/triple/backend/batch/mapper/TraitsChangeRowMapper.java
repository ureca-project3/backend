package com.triple.backend.batch.mapper;

import com.triple.backend.batch.dto.TraitsChangeDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TraitsChangeRowMapper implements RowMapper<TraitsChangeDto> {
    @Override
    public TraitsChangeDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        TraitsChangeDto traits = new TraitsChangeDto();
        traits.setTraitChangeId(rs.getLong("trait_change_id"));
        traits.setChangeAmount(rs.getInt("change_amount"));
        traits.setChildId(rs.getLong("child_id"));
        traits.setTraitId(rs.getLong("trait_id"));
        return traits;
    }
}
