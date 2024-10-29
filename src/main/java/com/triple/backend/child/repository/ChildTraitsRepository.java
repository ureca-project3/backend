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
            "WHERE ct.mbtiHistory.child.childId = :childId " +
            "AND ct.mbtiHistory.historyId = :historyId " +
            "AND ct.trait.test.testId = :testId " +
            "ORDER BY ct.childTraitsId ASC")
    List<TraitDataResponseDto> findTraitsByChildAndTest(Long childId, Long historyId, Long testId);

    @Query("SELECT ct FROM ChildTraits ct JOIN FETCH ct.trait " +
            "WHERE ct.mbtiHistory.historyId = :historyId " +
            "ORDER BY ct.mbtiHistory.createdAt DESC")
    List<ChildTraits> findLatestByMbtiHistory_HistoryIdWithTraits(@Param("historyId") Long historyId);

    @Query("SELECT ct FROM ChildTraits ct JOIN FETCH ct.trait " +
            "WHERE ct.mbtiHistory.historyId = :historyId " +
            "ORDER BY ct.trait.traitId ASC, ct.createdAt DESC")
    List<ChildTraits> findByMbtiHistory_HistoryIdWithTraits(@Param("historyId") Long historyId);

    @Query("select ct from ChildTraits ct where ct.mbtiHistory.child.childId = :childId")
    Optional<ChildTraits> findByChildId(@Param(value = "childId") Long childId);

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