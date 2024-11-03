package com.triple.backend.event.dto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventApplyResponseDto {
    private boolean success;
    private String message;
    private Long participantNumber;

    @Builder
    public EventApplyResponseDto(boolean success, String message, Long participantNumber) {
        this.success = success;
        this.message = message;
        this.participantNumber = participantNumber;
    }
}
