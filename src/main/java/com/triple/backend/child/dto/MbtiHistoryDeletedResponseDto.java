package com.triple.backend.child.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MbtiHistoryDeletedResponseDto {

    private Boolean isDeleted;

    public MbtiHistoryDeletedResponseDto(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
