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

import java.util.*;

@Component
@RequiredArgsConstructor
public class FeedbackReader implements ItemReader<List<BookChildTraitDto>> {

    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    private static final int PAGE_SIZE = 100;
    private int currentPage = 0;

    @Qualifier("mainNamedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate namedTemplate;
    private final HashOperations<String, String, Set<Long>> hashOperations;

    @Override
    public List<BookChildTraitDto> read() throws Exception {
        // 1. 좋아요 및 싫어요 데이터를 한 번에 가져옴
        Map<String, Set<Long>> likesMap = hashOperations.entries(LIKE_HASH_KEY);
        Map<String, Set<Long>> hatesMap = hashOperations.entries(HATE_HASH_KEY);

        // 좋아요 및 싫어요 데이터를 기반으로 childId와 bookId 목록 생성
        List<Long> childIds = new ArrayList<>();
        List<Long> bookIds = new ArrayList<>();

        likesMap.forEach((childId, likedBooks) -> {
            childIds.add(Long.valueOf(childId));
            bookIds.addAll(likedBooks);
        });

        hatesMap.forEach((childId, hatedBooks) -> {
            childIds.add(Long.valueOf(childId));
            bookIds.addAll(hatedBooks);
        });

        List<BookChildTraitDto> dtoList = fetchPage(childIds, bookIds, likesMap, hatesMap);
        // 더 이상 처리할 데이터가 없으면 종료
        return dtoList.isEmpty() ? null : dtoList;
    }

    private List<BookChildTraitDto> fetchPage(List<Long> childIds, List<Long> bookIds,
                                              Map<String, Set<Long>> likesMap,
                                              Map<String, Set<Long>> hatesMap) {
        int offset = currentPage * PAGE_SIZE;
        currentPage++;

        String childQuery = """
                SELECT ct.child_traits_id, ct.trait_score
                mh.history_id, mh.child_id
                t.trait_id, t.trait_name
                FROM child_traits ct
                JOIN mbti_history mh ON ct.history_id = mh.history_id
                JOIN trait t ON ct.trait_id = t.trait_id
                WHERE mh.child_id IN (:childIds)
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource childTraitParams = new MapSqlParameterSource();
        childTraitParams.addValue("childIds", childIds);
        childTraitParams.addValue("limit", PAGE_SIZE);
        childTraitParams.addValue("offset", offset);

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

        String traitChangeQuery = """
                SELECT tc.child_id, tc.trait_id, tc.change_amount
                FROM traits_change tc
                WHERE tc.child_id = :childId
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource traitsChangeParams = new MapSqlParameterSource();
        traitsChangeParams.addValue("childIds", childIds);
        traitsChangeParams.addValue("limit", PAGE_SIZE);
        traitsChangeParams.addValue("offset", offset);

        // traitChangeArray를 가져올 리스트 생성
        Map<Long, int[]> traitChangeMap = new HashMap<>();

        namedTemplate.query(
                traitChangeQuery,
                traitsChangeParams,
                (rs) -> {
                    Long childId = rs.getLong("child_id");
                    int traitId = rs.getInt("trait_id") - 1;
                    int changeAmount = rs.getInt("change_amount");

                    // traitChangeArray를 생성하고, traitId에 따라 값을 넣음
                    traitChangeMap.putIfAbsent(childId, new int[4]);
                    traitChangeMap.get(childId)[traitId] = changeAmount;
                }
        );

        String bookQuery = """
                SELECT bt.book_traits_id, bt.trait_score
                b.book_id
                t.trait_id, t.trait_name
                FROM book_traits bt
                JOIN book b ON bt.book_id = b.book_id
                JOIN trait t ON bt.trait_id = t.trait_id
                WHERE b.book_id IN (:bookIds)
                LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource bookTraitParams = new MapSqlParameterSource();
        bookTraitParams.addValue("bookIds", bookIds);
        bookTraitParams.addValue("limit", PAGE_SIZE);
        bookTraitParams.addValue("offset", offset);

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

        List<BookChildTraitDto> dtoList = new ArrayList<>();

        for (Long childId : childIds) {
            // Child의 4가지 trait 점수를 담을 배열 생성
            int[] childTraitsArray = new int[4];
            Long historyId = null;  // historyId를 저장할 변수

            for (ChildTraitsDto childTraitDto : childTraitsDtos) {
                if (childTraitDto.getChildId().equals(childId)) {
                    // traitId가 1, 2, 3, 4일 테니, 0, 1, 2, 3이 되도록 -1
                    int traitIndex = childTraitDto.getTraitId().intValue() - 1;
                    childTraitsArray[traitIndex] = childTraitDto.getTraitScore();
                    historyId = childTraitDto.getHistoryId();
                }
            }

            int[] traitChangeArray = traitChangeMap.getOrDefault(childId, new int[4]);

            // 좋아요 및 싫어요 책들의 Trait 정보를 담을 리스트 준비
            List<int[]> likedBookTraitsList = new ArrayList<>();
            List<int[]> hatedBookTraitsList = new ArrayList<>();

            // 좋아요한 책에 대한 처리
            Set<Long> likedBooks = likesMap.get(String.valueOf(childId));
            if (likedBooks != null) {
                for (Long bookId : likedBooks) {
                    int[] likedBookTraitsArray = new int[4];
                    for (BookTraitsDto bookTraitDto : bookTraitsDtos) {
                        if (bookTraitDto.getBookId().equals(bookId)) {
                            int traitIndex = bookTraitDto.getTraitId().intValue() - 1;
                            likedBookTraitsArray[traitIndex] = bookTraitDto.getTraitScore();
                        }
                    }
                    likedBookTraitsList.add(likedBookTraitsArray);
                }
            }

            // 싫어요한 책에 대한 처리
            Set<Long> hatedBooks = hatesMap.get(String.valueOf(childId));
            if (hatedBooks != null) {
                for (Long bookId : hatedBooks) {
                    int[] hatedBookTraitsArray = new int[4];
                    for (BookTraitsDto bookTraitDto : bookTraitsDtos) {
                        if (bookTraitDto.getBookId().equals(bookId)) {
                            int traitIndex = bookTraitDto.getTraitId().intValue() - 1;
                            hatedBookTraitsArray[traitIndex] = bookTraitDto.getTraitScore();
                        }
                    }
                    hatedBookTraitsList.add(hatedBookTraitsArray);
                }
            }

            // BookChildTraitDto 생성 및 리스트에 추가
            BookChildTraitDto dto = new BookChildTraitDto(
                    childId,
                    historyId,
                    childTraitsArray,
                    traitChangeArray,
                    likedBookTraitsList,
                    hatedBookTraitsList
            );

            dtoList.add(dto);
        }
        return dtoList;
    }
}


