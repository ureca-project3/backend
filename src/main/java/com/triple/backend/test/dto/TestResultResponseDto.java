package com.triple.backend.test.dto;

import com.triple.backend.test.entity.MbtiType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TestResultResponseDto {

    List<TraitDataResponseDto> traitDataResponseDtoList;
    MbtiType mbtiName;
    String mbtiDescription;
    String mbtiImage;
    String mbtiPhrase;

    public TestResultResponseDto(List<TraitDataResponseDto> traitDataResponseDtoList, MbtiType mbtiName, String mbtiDescription, String mbtiImage, String mbtiPhrase) {
        this.traitDataResponseDtoList = traitDataResponseDtoList;
        this.mbtiName = mbtiName;
        this.mbtiDescription = mbtiDescription;
        this.mbtiImage = mbtiImage;
        this.mbtiPhrase = mbtiPhrase;
    }
}
