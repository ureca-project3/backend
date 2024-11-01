package com.triple.backend.event.dto;

import com.triple.backend.event.entity.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventRequestDto {
    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long winnerCnt;
    private Long totalCnt;
    private LocalDateTime announceTime;

    public EventRequestDto(String eventName, LocalDateTime startTime, LocalDateTime endTime, Long winnerCnt, Long totalCnt, LocalDateTime announceTime) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.winnerCnt = winnerCnt;
        this.totalCnt = totalCnt;
        this.announceTime = announceTime;
    }

}
