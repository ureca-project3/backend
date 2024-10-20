package com.triple.backend.test.repository;

import com.triple.backend.test.entity.TestParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestParticipationRepository extends JpaRepository<TestParticipation, Long> {
    TestParticipation findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);
}
