package com.triple.backend.test.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TestResultDto {

    List<TraitDataDto> traitDataDtoList;

    public TestResultDto(List<TraitDataDto> traitDataDtoList) {
        this.traitDataDtoList = traitDataDtoList;
    }

}
