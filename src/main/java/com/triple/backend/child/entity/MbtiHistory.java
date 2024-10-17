package com.triple.backend.child.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class MbtiHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @JoinColumn(name = "child_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Child child;

    private String currentMbti;

    private LocalDateTime createdAt;

    private String reason;

    private boolean isDeleted = false;
}
