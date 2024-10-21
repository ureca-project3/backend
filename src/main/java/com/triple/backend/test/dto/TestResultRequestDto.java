package com.triple.backend.test.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TestResultRequestDto {

    List<TraitDataResponseDto> traitDataResponseDtoList;

    public TestResultRequestDto(List<TraitDataResponseDto> traitDataResponseDtoList) {
        this.traitDataResponseDtoList = traitDataResponseDtoList;
    }

}
