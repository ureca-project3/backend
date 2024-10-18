package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTest(Test test);

}
