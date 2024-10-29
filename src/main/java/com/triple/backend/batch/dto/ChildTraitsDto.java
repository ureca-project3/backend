package com.triple.backend.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@Getter
public class ChildTraitsDto {
    private Long childTraitsId;
    private Integer traitScore;
    private Long traitId;
}
