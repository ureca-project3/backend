package com.triple.backend.child.repository;

import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MbtiHistoryRepository extends JpaRepository<MbtiHistory, Long> {
  
    MbtiHistory findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    // 삭제될 히스토리 찾는 코드
    List<MbtiHistory> findByReasonAndIsDeleted(String reason, boolean isDeleted);

    // MBTI 히스토리 논리적 삭제 시 히스토리 1개인지 조회
    long count();

    @Query(value = "select mh from MbtiHistory mh left join ChildTraits ct " +
            "on mh.historyId = ct.mbtiHistory.historyId where ct.mbtiHistory.historyId is null")
    List<MbtiHistory> findNotHavingChildTraits();
}
