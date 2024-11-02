package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.Trait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    // 테스트의 성향 정보 조회
    @Query (value = "select t from Trait t where t.test = :test order by t.traitId")
    List<Trait> findByTest(Test test);

    // 테스트 ID에 해당하는 MBTI 키 맵 조회 (예시)
    @Query("select t.traitId, t.traitName from Trait t where t.test.testId = :testId")
    Map<Long, String> findMbtiKeyMapByTestId(Long testId);

}
