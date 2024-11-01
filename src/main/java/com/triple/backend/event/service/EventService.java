package com.triple.backend.event.service;

import com.triple.backend.event.dto.EventApplyRequestDto;
import com.triple.backend.event.dto.EventApplyResponseDto;
import com.triple.backend.event.dto.EventRequestDto;
import com.triple.backend.event.dto.EventResultResponseDto;

public interface EventService {
    EventResultResponseDto getEventWinner(Long eventId);
    EventApplyResponseDto applyEvent(EventApplyRequestDto request);
    void insertEvent(EventRequestDto eventRequestDto);
}
