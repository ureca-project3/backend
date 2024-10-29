package com.triple.backend.batch.mapper;

import com.triple.backend.batch.dto.BookTraitsDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BookTraitsRowMapper implements RowMapper<BookTraitsDto> {
    @Override
    public BookTraitsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        BookTraitsDto traits = new BookTraitsDto();
        traits.setBookTraitsId(rs.getLong("book_traits_id"));
        traits.setBookTraitScore(rs.getInt("trait_score"));
        traits.setTraitId(rs.getLong("trait_id"));
        return traits;
    }
}
