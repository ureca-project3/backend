package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.Trait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    // 테스트의 성향 정보 조회
    List<Trait> findByTest(Test test);

}
