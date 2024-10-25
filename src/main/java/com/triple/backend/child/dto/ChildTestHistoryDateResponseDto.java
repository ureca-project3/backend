package com.triple.backend.child.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public class ChildTestHistoryDateResponseDto {
    private Long historyId;
    private Map<String, Integer> historyMbti;
    private String currentMbti;
    private String mbtiPhrase;
    private String mbtiDescription;
    private String mbtiImage;

    public ChildTestHistoryDateResponseDto(Long historyId, Map<String, Integer> historyMbti, String currentMbti,
                                           String mbtiPhrase, String mbtiDescription, String mbtiImage) {
        this.historyId = historyId;
        this.historyMbti = historyMbti;
        this.currentMbti = currentMbti;
        this.mbtiPhrase = mbtiPhrase;
        this.mbtiDescription = mbtiDescription;
        this.mbtiImage = mbtiImage;
    }
}
