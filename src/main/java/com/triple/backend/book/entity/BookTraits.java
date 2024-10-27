package com.triple.backend.book.entity;

import com.triple.backend.test.entity.Trait;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class BookTraits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookTraitsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trait_id")
    private Trait trait;

    private Integer traitScore;

    @Builder
    public BookTraits(Book book, Trait trait, Integer traitScore) {
        this.book = book;
        this.trait = trait;
        this.traitScore = traitScore;
    }
}
