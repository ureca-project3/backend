package com.triple.backend.child.entity;

import jakarta.persistence.*;
import lombok.Builder;
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

    @Builder
    public MbtiHistory(Child child, String currentMbti, LocalDateTime createdAt, String reason, boolean isDeleted) {
        this.child = child;
        this.currentMbti = currentMbti;
        this.createdAt = createdAt;
        this.reason = reason;
        this.isDeleted = isDeleted;
    }

    // 업데이트
    public void updateDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
