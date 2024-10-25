package com.triple.backend.test.repository;

import com.triple.backend.test.entity.TestAnswer;
import com.triple.backend.test.entity.TestAnswerId;
import com.triple.backend.test.entity.TestParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestAnswerRepository extends JpaRepository<TestAnswer, TestAnswerId> {

    List<TestAnswer> findByTestAnswerIdTestParticipationIn(List<TestParticipation> testParticipationList);
}
