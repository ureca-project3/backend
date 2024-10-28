package com.triple.backend.event.service.impl;

import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.event.dto.EventResponseDto;
import com.triple.backend.event.entity.Event;
import com.triple.backend.event.entity.EventQuestion;
import com.triple.backend.event.repository.EventQuestionRepository;
import com.triple.backend.event.repository.EventRepository;
import com.triple.backend.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventQuestionRepository eventQuestionRepository;


    @Override
    public EventResponseDto getEvent(Long eventId) {

        Event event = eventRepository.findById(eventId).orElseThrow( () -> NotFoundException.entityNotFound("이벤트"));
        List<EventQuestion> eventQuestionList = eventQuestionRepository.findByEvent(event);
        Map<Long, String> eventQuestionMap = new LinkedHashMap<>();

        for (EventQuestion eventQuestion : eventQuestionList) {
            eventQuestionMap.put(eventQuestion.getEventQuestionId(), eventQuestion.getEventQText());
        }

        return EventResponseDto.builder()
                .eventId(eventId)
                .eventName(event.getEventName())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .winnerCnt(event.getWinnerCnt())
                .announceTime(event.getAnnounceTime())
                .status(event.isStatus())
                .eventQuestion(eventQuestionMap)
                .build();
    }
}
