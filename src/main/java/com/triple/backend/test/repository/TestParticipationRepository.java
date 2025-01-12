package com.triple.backend.test.repository;

import com.triple.backend.child.entity.Child;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestParticipationRepository extends JpaRepository<TestParticipation, Long> {
    
    TestParticipation findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    // 성향 진단 테스트 참여 여부 조회
    TestParticipation findTopByChildAndTestOrderByCreatedAtDesc(Child child, Test test);

}
