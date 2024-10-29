package com.triple.backend.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EventResultResponseDto {

    private List<WinnerResponseDto> winnerList;

    public EventResultResponseDto(List<WinnerResponseDto> winnerList) {
        this.winnerList = winnerList;
    }
}
