package com.triple.backend.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@Setter
@Getter
public class MbtiDto {
    private Long childId;
    private String currentMbti;
    private String changeReason;
    private Long changeReasonId;
    private Boolean isDeleted;
}
