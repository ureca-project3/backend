package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.Trait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    // 테스트의 성향 정보 조회
    @Query (value = "select t from Trait t where t.test = :test order by t.traitId")
    List<Trait> findByTest(Test test);

    @Query("SELECT t FROM Trait t WHERE t.traitName = :traitName")
    Trait findByTraitName(String traitName);
}
