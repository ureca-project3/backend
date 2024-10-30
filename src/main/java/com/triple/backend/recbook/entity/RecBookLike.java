package com.triple.backend.recbook.entity;

import com.triple.backend.child.entity.Child;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class RecBookLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recBookLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private RecBook recBook;

    private LocalDateTime createdAt;

    @Builder
    public RecBookLike(Child child, RecBook recBook) {
        this.child = child;
        this.recBook = recBook;
        this.createdAt = LocalDateTime.now();
    }
}
