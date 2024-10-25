package com.triple.backend.test.repository;

import com.triple.backend.test.entity.TestAnswer;
import com.triple.backend.test.entity.TestAnswerId;
import com.triple.backend.test.entity.TestParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, TestAnswerId> {

    // 자녀 히스토리 물리적 삭제 시 기타 데이터 삭제
    void deleteByTestAnswerId(TestAnswerId testAnswerId);

    List<TestAnswer> findByTestAnswerIdTestParticipationIn(List<TestParticipation> testParticipationList);
}
