package com.triple.backend.event.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class EventQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventQuestionId;

    @Column(name = "event_q_text")
    private String eventQText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}
