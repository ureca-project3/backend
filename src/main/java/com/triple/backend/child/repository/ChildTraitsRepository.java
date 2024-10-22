package com.triple.backend.child.repository;

import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.test.dto.TraitDataResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChildTraitsRepository extends JpaRepository<ChildTraits, Long> {

    @Query("SELECT new com.triple.backend.test.dto.TraitDataResponseDto(t.traitName, t.traitDescription, ct.traitScore) " +
            "FROM ChildTraits ct JOIN ct.trait t " +
            "WHERE ct.mbtiHistory.child.childId = :childId AND ct.mbtiHistory.historyId = :historyId AND ct.trait.test.testId = :testId")
    List<TraitDataResponseDto> findTraitsByChildAndTest(Long childId, Long historyId, Long testId);

    // N+1 문제 해결 JOIN FETCH로 해결
    @Query("SELECT ct FROM ChildTraits ct JOIN FETCH ct.trait WHERE ct.mbtiHistory.historyId = :historyId")
    List<ChildTraits> findByMbtiHistory_HistoryIdWithTraits(@Param("historyId") Long historyId);

}

