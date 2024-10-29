package com.triple.backend.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WinnerResponseDto {

    private String winnerName;
    private String phoneNumber;

    public WinnerResponseDto(String winnerName, String phoneNumber) {
        this.winnerName = winnerName;
        this.phoneNumber = phoneNumber;
    }
}
