package com.triple.backend.event.repository;

import com.triple.backend.event.entity.Event;
import com.triple.backend.event.entity.EventQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventQuestionRepository extends JpaRepository<EventQuestion, Long> {

    // 이벤트 질문 조회
    List<EventQuestion> findByEvent(Event event);

}
