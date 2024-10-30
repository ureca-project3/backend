package com.triple.backend.event.repository;

import com.triple.backend.event.entity.EventQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventQuestionRepository extends JpaRepository<EventQuestion, Long> {
}
