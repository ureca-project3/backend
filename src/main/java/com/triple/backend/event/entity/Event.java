package com.triple.backend.event.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
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
}
