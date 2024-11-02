package com.triple.backend.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
public class EventPartRequestDto {

    private Long eventId;
    private Long memberId;
    private String name;
    private String phone;
    private LocalDateTime createdAt;
    private Map<String, Integer> answerList;

    @Builder
    public EventPartRequestDto(Long eventId, Long memberId, String name, String phone,
                               LocalDateTime createdAt, Map<String, Integer> answerList) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
        this.answerList = answerList;
    }

}
