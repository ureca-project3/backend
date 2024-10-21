package com.triple.backend.child.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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

    public MbtiHistory(Long historyId, Child child, String currentMbti, LocalDateTime createdAt, String reason, boolean isDeleted) {
        this.historyId = historyId;
        this.child = child;
        this.currentMbti = currentMbti;
        this.createdAt = createdAt;
        this.reason = reason;
        this.isDeleted = isDeleted;
    }

}
