package com.triple.backend.event.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Winning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long winningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_part_id")
    private EventPart eventPart;
}
