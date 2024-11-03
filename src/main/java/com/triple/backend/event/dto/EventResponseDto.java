package com.triple.backend.event.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
public class EventResponseDto {

    Long eventId;
    String eventName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Long winnerCnt;
    LocalDateTime announceTime;
    boolean status;
    Map<Long, String> eventQuestion;

    @Builder
    public EventResponseDto(Long eventId, String eventName, LocalDateTime startTime, LocalDateTime endTime,
                            Long winnerCnt, LocalDateTime announceTime, boolean status, Map<Long, String> eventQuestion) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.winnerCnt = winnerCnt;
        this.announceTime = announceTime;
        this.status = status;
        this.eventQuestion = eventQuestion;
    }

}
