package com.triple.backend.child.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ChildTestHistoryResponseDto {
    private List<String> currentMbti;
    private List<String> createdAt;
    private Map<String,String> traitData;

    public ChildTestHistoryResponseDto(List<String> currentMbti, List<String> createdAt, Map<String,String> traitData){
        this.currentMbti = currentMbti;
        this.createdAt = createdAt;
        this.traitData = traitData;
    }
}