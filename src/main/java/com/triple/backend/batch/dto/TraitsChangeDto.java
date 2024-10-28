package com.triple.backend.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TraitsChangeDto {
    private Long traitChangeId;
    private Long childId;
    private Long traitId;
    private Integer changeAmount;
}
