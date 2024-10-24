package com.triple.backend.child.repository;

import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface MbtiHistoryRepository extends JpaRepository<MbtiHistory, Long> {
  
    MbtiHistory findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    @Modifying
    @Query("DELETE FROM ChildTraits ct WHERE ct.mbtiHistory.historyId IN (SELECT m.historyId FROM MbtiHistory m WHERE m.isDeleted = true AND m.modifiedAt < :date)")
    void deleteOldChildTraits(LocalDateTime date);

    @Modifying
    @Query("DELETE FROM MbtiHistory m WHERE m.isDeleted = true AND m.modifiedAt < :date")
    void deleteOldMbtiHistories(LocalDateTime date);

}
