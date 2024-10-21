package com.triple.backend.child.repository;

import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MbtiHistoryRepository extends JpaRepository<MbtiHistory, Long> {

    MbtiHistory findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    List<MbtiHistory> findByChild_ChildIdOrderByCreatedAtDesc(Long childId);
}
