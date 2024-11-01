package com.triple.backend.event.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String eventName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long winnerCnt;

    private Long totalCnt;

    private LocalDateTime announceTime;

    private boolean status;

    @Builder
    public Event (String eventName, LocalDateTime startTime, LocalDateTime endTime, Long winnerCnt, Long totalCnt, LocalDateTime announceTime, boolean status) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.winnerCnt = winnerCnt;
        this.totalCnt = totalCnt;
        this.announceTime = announceTime;
        this.status = status;
    }

    // 총 참여자 수 업데이트
    public void updateTotalCnt(Long totalCnt) { this.totalCnt = totalCnt; }

}
