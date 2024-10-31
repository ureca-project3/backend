package com.triple.backend.event.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EventApplyResponseDto {
    private final boolean success;
    private final String message;
    private final Long participantNumber;

    public static EventApplyResponseDto success(Long participantNumber) {
        return EventApplyResponseDto.builder()
                .success(true)
                .message("참여가 완료되었습니다")
                .participantNumber(participantNumber)
                .build();
    }

    public static EventApplyResponseDto failed(String message) {
        return EventApplyResponseDto.builder()
                .success(true)
                .message(message)
                .build();
    }
}
