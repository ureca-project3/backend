package com.triple.backend.test.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TraitDataDto {

    String traitName;
    String traitDescription;
    Integer traitScore;

    public TraitDataDto(String traitName, String traitDescription, Integer traitScore) {
        this.traitName = traitName;
        this.traitDescription = traitDescription;
        this.traitScore = traitScore;
    }

}
