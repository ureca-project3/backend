package com.triple.backend.event.service;

import com.triple.backend.event.dto.EventResponseDto;

public interface EventService {

    // 선착순 응모 이벤트 조회
    EventResponseDto getEvent(Long eventId);

}
