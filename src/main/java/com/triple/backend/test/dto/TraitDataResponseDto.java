package com.triple.backend.test.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TraitDataResponseDto {

    String traitName;
    String traitDescription;
    Integer traitScore;

    public TraitDataResponseDto(String traitName, String traitDescription, Integer traitScore) {
        this.traitName = traitName;
        this.traitDescription = traitDescription;
        this.traitScore = traitScore;
    }

}
