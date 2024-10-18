package com.triple.backend.event.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.*;
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

    private String comment;
}
