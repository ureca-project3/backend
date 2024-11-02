package com.triple.backend.event.service;

import com.triple.backend.event.dto.EventApplyRequestDto;
import com.triple.backend.event.dto.EventApplyResponseDto;
import com.triple.backend.event.dto.EventRequestDto;
import com.triple.backend.event.dto.EventResultResponseDto;

import com.triple.backend.event.dto.EventResponseDto;

public interface EventService {
    EventResultResponseDto getEventWinner(Long eventId);
    EventApplyResponseDto applyEvent(EventApplyRequestDto request);
    void insertEvent(EventRequestDto eventRequestDto);

    // 선착순 응모 이벤트 조회
    EventResponseDto getEvent(Long eventId);

    String getEventQuestion(Long eventId);

    Object getEventList();
}
