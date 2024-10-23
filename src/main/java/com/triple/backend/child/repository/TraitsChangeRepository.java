package com.triple.backend.child.repository;

import com.triple.backend.child.entity.TraitsChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TraitsChangeRepository extends JpaRepository<TraitsChange, Long> {

    @Query(value = "select tc from TraitsChange tc where tc.child.childId = :childId and tc.trait.traitId = :traitId")
    Optional<TraitsChange> findByChildIdAndTraitId(Long childId, Long traitId);
}
