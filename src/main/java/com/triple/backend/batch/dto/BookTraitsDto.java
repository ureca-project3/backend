package com.triple.backend.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class BookTraitsDto {
    private Long bookTraitsId;
    private Integer bookTraitScore;
    private Long traitId;
}
