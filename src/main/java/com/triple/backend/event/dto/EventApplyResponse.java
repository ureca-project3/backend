package com.triple.backend.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
public class EventApplyResponse {
    private final boolean success;
    private final String message;
    private final Long participantNumber;

    public static EventApplyResponse success(Long participantNumber) {
        return EventApplyResponse.builder()
                .success(true)
                .message("참여가 완료되었습니다")
                .participantNumber(participantNumber)
                .build();
    }

    public static EventApplyResponse failed(String message) {
        return EventApplyResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
