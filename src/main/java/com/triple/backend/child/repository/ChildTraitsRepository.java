package com.triple.backend.child.repository;

import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.test.dto.TraitDataDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChildTraitsRepository extends JpaRepository<ChildTraits, Long> {

    @Query("SELECT new com.triple.backend.test.dto.TraitDataDto(t.traitName, t.traitDescription, ct.traitScore) " +
            "FROM ChildTraits ct JOIN ct.trait t " +
            "WHERE ct.mbtiHistory.child.childId = :childId AND ct.mbtiHistory.historyId = :historyId AND ct.trait.test.testId = :testId")
    List<TraitDataDto> findTraitsByChildAndTest(Long childId, Long historyId, Long testId);
}
