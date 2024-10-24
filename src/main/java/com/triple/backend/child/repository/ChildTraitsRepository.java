package com.triple.backend.child.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.test.dto.TraitDataResponseDto;

public interface ChildTraitsRepository extends JpaRepository<ChildTraits, Long> {

    @Query("SELECT new com.triple.backend.test.dto.TraitDataResponseDto(t.traitName, t.traitDescription, ct.traitScore) " +
            "FROM ChildTraits ct JOIN ct.trait t " +
            "WHERE ct.mbtiHistory.child.childId = :childId AND ct.mbtiHistory.historyId = :historyId AND ct.trait.test.testId = :testId")
    List<TraitDataResponseDto> findTraitsByChildAndTest(Long childId, Long historyId, Long testId);

    @Query(value = "SELECT ct FROM ChildTraits ct WHERE ct.mbtiHistory.child.childId = :childId")
    Optional<ChildTraits> findChildTraitsByChildId(Long childId);
}
