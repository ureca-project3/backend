package com.triple.backend.child.entity;

import com.triple.backend.test.entity.Trait;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ChildTraits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long childTraitsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id")
    private MbtiHistory mbtiHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trait_id")
    private Trait trait;

    private Integer traitScore;

    private LocalDateTime createdAt;

    @Builder
    public ChildTraits(MbtiHistory mbtiHistory, Trait trait, Integer traitScore, LocalDateTime createdAt) {
        this.mbtiHistory = mbtiHistory;
        this.trait = trait;
        this.traitScore = traitScore;
        this.createdAt = createdAt;
    }

}
