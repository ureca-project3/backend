package com.triple.backend.child.repository;

import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MbtiHistoryRepository extends JpaRepository<MbtiHistory, Long> {

    MbtiHistory findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);
}
