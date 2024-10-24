package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChildTraitsDto {
    private Long childTraitId;
    private Long childId;
    private Long historyId;
    private Long traitId;
    private String traitName;
    private Integer traitScore;
}
