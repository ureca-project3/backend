package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.BookChildTraitDto;
import com.triple.backend.batch.dto.BookTraitsDto;
import com.triple.backend.batch.dto.ChildTraitsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FeedbackReader implements ItemReader<List<BookChildTraitDto>> {

    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    private final HashOperations<String, String, Set<Long>> hashOperations;

    @Qualifier("mainDataSource")
    private final DataSource mainDataSource;

    @Override
    public List<BookChildTraitDto> read() throws Exception {
        List<BookChildTraitDto> dtoList = new ArrayList<>();

        // 1. 좋아요 및 싫어요 데이터를 한 번에 가져옴
        Map<String, Set<Long>> likesMap = hashOperations.entries(LIKE_HASH_KEY);
        Map<String, Set<Long>> hatesMap = hashOperations.entries(HATE_HASH_KEY);

        // 좋아요 데이터 처리 - 좋아요한 아이들의 아이디를 모음
        List<Long> childIds = new ArrayList<>();
        // 아이들이 좋아요한 책 아이디를 모음
        List<Long> bookIds = new ArrayList<>();

        likesMap.forEach((childId, likedBooks) -> {
            childIds.add(Long.valueOf(childId));
            bookIds.addAll(likedBooks);
        });

        hatesMap.forEach((childId, hatedBooks) -> {
            childIds.add(Long.valueOf(childId));
            bookIds.addAll(hatedBooks);
        });

        // 2. childIds에 해당하는 아이들의 Trait 정보를 한 번에 조회
        // 일단 필요해 보이는 건 다 가지고 옴
        String childQuery = "SELECT ct.child_traits_id, ct.trait_score, " +
                "mh.history_id, mh.child_id, " +
                "t.trait_id, t.trait_name " +
                "FROM child_traits ct " +
                "JOIN mbti_history mh ON ct.history_id = mh.history_id " +
                "JOIN trait t ON ct.trait_id = t.trait_id " +
                "WHERE mh.child_id IN (:childIds)";

        MapSqlParameterSource childTraitParams = new MapSqlParameterSource();
        childTraitParams.addValue("childIds", childIds);

        NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(mainDataSource);
        List<ChildTraitsDto> childTraitsDtos = namedTemplate.query(
                childQuery,
                childTraitParams,
                (rs, rowNum) -> new ChildTraitsDto(
                        rs.getLong("child_traits_id"),
                        rs.getLong("child_id"),
                        rs.getLong("history_id"),
                        rs.getLong("trait_id"),
                        rs.getString("trait_name"),
                        rs.getInt("trait_score")
                )
        );

        // 3. bookIds에 해당하는 책들의 Trait 정보를 한 번에 조회
        String bookQuery = "SELECT bt.book_traits_id, bt.trait_score, " +
                "b.book_id, " +
                "t.trait_id, t.trait_name " +
                "FROM book_traits bt " +
                "JOIN book b ON bt.book_id = b.book_id " +
                "JOIN trait t ON bt.trait_id = t.trait_id " +
                "WHERE b.book_id IN (:bookIds)";

        MapSqlParameterSource bookTraitParams = new MapSqlParameterSource();
        bookTraitParams.addValue("bookIds", bookIds);

        namedTemplate = new NamedParameterJdbcTemplate(mainDataSource);
        List<BookTraitsDto> bookTraitsDtos = namedTemplate.query(
                bookQuery,
                bookTraitParams,
                (rs, rowNum) -> new BookTraitsDto(
                        rs.getLong("book_id"),
                        rs.getLong("trait_id"),
                        rs.getString("trait_name"),
                        rs.getInt("trait_score")
                )
        );

        // 4. 각각의 childId에 맞는 책 정보를 결합해 dto 생성
        // 위에서 받아온 아이들의 성향을 모아놓은 dto를 하나씩 돌면서 -> 한 명의 정보
        for (ChildTraitsDto childTraitDto : childTraitsDtos) {
            Long childId = childTraitDto.getChildId();
            // 위에서 redis 에서 가져온 좋아요/싫어요 책들을 각각 넣고
            Set<Long> likedBooks = likesMap.get(String.valueOf(childId));  // 좋아요한 책들
            Set<Long> hatedBooks = hatesMap.get(String.valueOf(childId));  // 싫어요한 책들

            // 각 책들의 Trait 정보를 넣을 List를 마련
            List<int[]> likedBookTraitsList = new ArrayList<>();
            List<int[]> hatedBookTraitsList = new ArrayList<>();

            // 좋아요한 책에 대한 처리
            if (likedBooks != null) {
                // 위에서 받아온 책 성향을 모아놓은 dto를 하나씩 돌면서 -> 한 책의 정보
                for (BookTraitsDto bookTraitDto : bookTraitsDtos) {
                    // 책ID가 좋아요한 책이 맞다면
                    if (likedBooks.contains(bookTraitDto.getBookId())) {
                        // *** 이 부분 확인 필요!
                        likedBookTraitsList.add(new int[]{bookTraitDto.getTraitScore()});
                    }
                }
            }

            // 싫어요한 책에 대한 처리
            if (hatedBooks != null) {
                for (BookTraitsDto bookTraitDto : bookTraitsDtos) {
                    if (hatedBooks.contains(bookTraitDto.getBookId())) {
                        hatedBookTraitsList.add(new int[]{bookTraitDto.getTraitScore()});
                    }
                }
            }

            // 아이와 책의 Trait 리스트를 기반으로 BookChildTraitDto 생성
            BookChildTraitDto dto = new BookChildTraitDto(
                    childId,
                    new int[]{childTraitDto.getTraitScore()},
                    likedBookTraitsList,
                    hatedBookTraitsList
            );

            dtoList.add(dto);

            // 100개를 초과하면 처리 중단
            if (dtoList.size() >= 100) break;
        }

        // 더 이상 처리할 데이터가 없으면 종료
        return dtoList.isEmpty() ? null : dtoList;
    }

}
