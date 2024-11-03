package com.triple.backend.event.service;

import com.triple.backend.event.dto.*;

public interface EventService {
    EventResultResponseDto getEventWinner(Long eventId);
    EventApplyResponseDto applyEvent(EventApplyRequestDto request);
    void insertEvent(EventRequestDto eventRequestDto);

    // 선착순 응모 이벤트 조회
    EventResponseDto getEvent(Long eventId);

    EventQuestionResponseDto getEventQuestion(Long eventId);

    Object getEventList();
}