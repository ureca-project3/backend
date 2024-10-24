package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateTraitChangeDto {
    private Long childId;
    private Long traitId;
    private Integer changeAmount;
}
