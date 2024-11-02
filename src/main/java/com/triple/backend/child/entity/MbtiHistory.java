package com.triple.backend.child.entity;

import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MbtiHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @JoinColumn(name = "child_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Child child;

    private String currentMbti;

    private String reason;

    private Long reasonId;

    private boolean isDeleted = false;

    @Builder
    public MbtiHistory(Long historyId, Child child, String currentMbti, String reason,Long reasonId, boolean isDeleted) {
        this.historyId = historyId;
        this.child = child;
        this.currentMbti = currentMbti;
        this.reason = reason;
        this.reasonId = reasonId;
        this.isDeleted = isDeleted;
    }

    // 삭제 상태 업데이트
    public void updateDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
