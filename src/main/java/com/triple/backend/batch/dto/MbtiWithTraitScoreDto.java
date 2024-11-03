package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class MbtiWithTraitScoreDto {
    private Long childId;
    private Long traitId;
    private Integer traitScore;
    private LocalDateTime createdAt;
    private String currentMbti;
    private Long historyId;
}
