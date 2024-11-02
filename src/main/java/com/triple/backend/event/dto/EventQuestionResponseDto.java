package com.triple.backend.event.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventQuestionResponseDto {
    private Long eventQuestionId;
    private String eventQText;
}