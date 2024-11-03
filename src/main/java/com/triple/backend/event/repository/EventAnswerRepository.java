package com.triple.backend.event.repository;

import com.triple.backend.event.entity.EventAnswer;
import com.triple.backend.event.entity.EventAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAnswerRepository extends JpaRepository<EventAnswer, EventAnswerId> {
}
