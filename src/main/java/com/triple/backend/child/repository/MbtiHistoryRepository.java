package com.triple.backend.child.repository;

import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.MbtiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MbtiHistoryRepository extends JpaRepository<MbtiHistory, Long> {
  
    MbtiHistory findTopByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    List<MbtiHistory> findByChild_ChildIdOrderByCreatedAtDesc(Long childId);

    // 날짜로 자녀 히스토리 조회
    MbtiHistory findByChildAndCreatedAt(Child child, LocalDateTime createdAt);

    @Query("SELECT mh FROM MbtiHistory mh WHERE mh.child.childId = :childId AND mh.reason = '진단 결과'")
    List<MbtiHistory> findResultsByChildId(@Param("childId") Long childId);

    // 삭제될 히스토리 찾는 코드
    List<MbtiHistory> findByReasonAndIsDeleted(String reason, boolean isDeleted);

    // MBTI 히스토리 논리적 삭제 시 히스토리 1개인지 조회
    long count();
}
