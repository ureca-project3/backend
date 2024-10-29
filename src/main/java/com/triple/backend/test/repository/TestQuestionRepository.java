package com.triple.backend.test.repository;

import com.triple.backend.test.dto.TestQuestionTraitResponseDto;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    // 자녀 성향 질문 조회
    List<TestQuestion> findByTest(Test test, Pageable pageable);

    // 자녀 성향 테스트 질문 및 질문 성향 조회
    @Query("SELECT new com.triple.backend.test.dto.TestQuestionTraitResponseDto(q.questionId, q.test, q.trait, q.questionText, t.traitName) " +
        "FROM TestQuestion q JOIN q.trait t WHERE q.questionId = :questionId ")
    TestQuestionTraitResponseDto findQuestionWithTraitById(@Param("questionId") Long questionId);
}
