package com.triple.backend.child.entity;

import com.triple.backend.test.entity.Trait;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
}
