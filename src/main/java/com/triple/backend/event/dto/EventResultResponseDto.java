package com.triple.backend.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EventResultResponseDto {

    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long winnerCnt;
    private LocalDateTime announceTime;
    private List<WinnerResponseDto> winnerList;

    @Builder
    public EventResultResponseDto(String eventName, LocalDateTime startTime, LocalDateTime endTime, Long winnerCnt,
                                  LocalDateTime announceTime, List<WinnerResponseDto> winnerList) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.winnerCnt = winnerCnt;
        this.announceTime = announceTime;
        this.winnerList = winnerList;
    }
}
