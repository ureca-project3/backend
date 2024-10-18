package com.triple.backend.test.entity;

import com.triple.backend.child.entity.Child;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class TestParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testParticipationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Child child;
}
