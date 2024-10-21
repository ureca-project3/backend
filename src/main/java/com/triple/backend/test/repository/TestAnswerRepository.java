package com.triple.backend.test.repository;

import com.triple.backend.test.entity.TestAnswer;
import com.triple.backend.test.entity.TestAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, TestAnswerId> {



}
