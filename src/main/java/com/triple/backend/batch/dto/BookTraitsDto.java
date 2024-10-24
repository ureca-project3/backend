package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BookTraitsDto {
    private Long bookId;
    private Long traitId;
    private String traitName;
    private Integer traitScore;
}
