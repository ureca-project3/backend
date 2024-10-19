package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    // 자녀 성향 질문 조회
    List<TestQuestion> findByTest(Test test, Pageable pageable);

}
