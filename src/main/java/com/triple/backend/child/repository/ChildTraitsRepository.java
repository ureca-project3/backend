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

    @Query("select ct from ChildTraits ct where ct.mbtiHistory.child.childId = :childId")
    Optional<ChildTraits> findByChildId(@Param(value = "childId") Long childId);

    List<ChildTraits> findByMbtiHistoryIn(List<MbtiHistory> mbtiHistories);
}