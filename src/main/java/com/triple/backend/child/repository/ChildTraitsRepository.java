package com.triple.backend.child.repository;

import java.util.List;
import java.util.Optional;

import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.test.dto.TraitDataResponseDto;
import org.springframework.data.repository.query.Param;

public interface ChildTraitsRepository extends JpaRepository<ChildTraits, Long> {

    @Query("SELECT new com.triple.backend.test.dto.TraitDataResponseDto(t.traitName, t.traitDescription, ct.traitScore) " +
            "FROM ChildTraits ct JOIN ct.trait t " +
            "WHERE ct.mbtiHistory.child.childId = :childId AND ct.mbtiHistory.historyId = :historyId AND ct.trait.test.testId = :testId")
    List<TraitDataResponseDto> findTraitsByChildAndTest(Long childId, Long historyId, Long testId);

    List<ChildTraits> findByMbtiHistoryIn(List<MbtiHistory> mbtiHistories);

    @Query(value = """
                SELECT ct.* 
                FROM child_traits ct
                INNER JOIN (
                    SELECT trait_id, MAX(created_at) AS max_created_at
                    FROM child_traits
                    WHERE history_id = :historyId
                    GROUP BY trait_id
                ) latest ON ct.trait_id = latest.trait_id AND ct.created_at = latest.max_created_at
                WHERE ct.history_id = :historyId
                ORDER BY ct.trait_id
            """, nativeQuery = true)
    List<ChildTraits> findLatestTraitsByHistoryId(@Param("historyId") Long historyId);

}