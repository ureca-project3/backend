package com.triple.backend.child.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class ChildHistoryResponseDto {
    private Map<String, Integer> historyMbti;
    private String reason;
    private String currentMbti;

    public ChildHistoryResponseDto(Map<String, Integer> historyMbti, String reason, String currentMbti) {
        this.historyMbti = historyMbti;
        this.reason = reason;
        this.currentMbti = currentMbti;
    }
}
