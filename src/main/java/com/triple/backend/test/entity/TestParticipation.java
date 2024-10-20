package com.triple.backend.test.entity;

import com.triple.backend.child.entity.Child;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
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

    private LocalDateTime createdAt;

    @Builder
    public TestParticipation(Test test, Child child, LocalDateTime createdAt) {
        this.test = test;
        this.child = child;
        this.createdAt = createdAt;
    }
}
