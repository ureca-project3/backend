package com.triple.backend.child.dto;

import com.triple.backend.test.dto.TraitDataResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ChildTestHistoryResponseDto {
    private Long historyId;
    private List<TraitDataResponseDto> historyMbti;
    private String currentMbti;
    private List<String> historyDate;
    private String mbtiPhrase;
    private String mbtiDescription;
    private String mbtiImage;

    @Builder
    public ChildTestHistoryResponseDto(Long historyId, List<TraitDataResponseDto> historyMbti, String currentMbti,
                                       List<String> historyDate, String mbtiPhrase, String mbtiDescription, String mbtiImage) {
        this.historyId = historyId;
        this.historyMbti = historyMbti;
        this.currentMbti = currentMbti;
        this.historyDate = historyDate;
        this.mbtiPhrase = mbtiPhrase;
        this.mbtiDescription = mbtiDescription;
        this.mbtiImage = mbtiImage;
    }
}