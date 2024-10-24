package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TraitsChangeDto {
    private Long childId;
    private Long traitId;
    private double[] changeAmount;
    private boolean isBeyondFive;
}
