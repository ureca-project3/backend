package com.triple.backend.event.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class EventPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventPartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private LocalDateTime createdAt;

    @Builder
    public EventPart(Long eventPartId, Member member, Event event, LocalDateTime createdAt) {
        this.eventPartId = eventPartId;
        this.member = member;
        this.event = event;
        this.createdAt = createdAt;
    }

}
