package com.triple.backend.child.repository;

import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MbtiHistoryRepository extends JpaRepository<MbtiHistory, Long> {

    Optional<MbtiHistory> findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    List<MbtiHistory> findByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    // 날짜로 자녀 히스토리 조회
    Optional<MbtiHistory> findByChildAndCreatedAt(Child child, LocalDateTime createdAt);

    // 삭제될 히스토리 찾는 코드
    List<MbtiHistory> findByReasonAndIsDeleted(String reason, boolean isDeleted);

    long countByChild_ChildId(Long childId);

    // 자녀 성향 히스토리 모음 조회 - 자녀 성향 진단 결과 중 가장 최신 히스토리 조회
    Optional<MbtiHistory> findTopByChildAndReasonAndIsDeletedFalseOrderByCreatedAtDesc(Child child, String reason);

    // 자녀 성향 히스토리 모음 조회 - 자녀 성향 진단 결과 날짜만 조회
    List<MbtiHistory> findByChildAndReasonOrderByCreatedAtDesc(Child child, String reason);

    @Query(value = "select mh from MbtiHistory mh left join ChildTraits ct " +
            "on mh.historyId = ct.mbtiHistory.historyId where ct.mbtiHistory.historyId is null")
    List<MbtiHistory> findNotHavingChildTraits();
}
