package com.triple.backend.child.entity;

import com.triple.backend.common.entity.BaseEntity;
import com.triple.backend.test.entity.Trait;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class TraitsChange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long traitChangeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trait_id")
    private Trait trait;

    private Double changeAmount;

    @Builder
    public TraitsChange(Child child, Trait trait, Double changeAmount) {
        this.child = child;
        this.trait = trait;
        this.changeAmount = changeAmount;
    }
}
