package com.triple.backend.book.dto;

import com.triple.backend.book.entity.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BookRankingResponseDto {
    private String title;
    private String recAge;
    private String imageUrl;
    private String publisher;

    public BookRankingResponseDto(Book book) {
        this.title = book.getTitle();
        this.recAge = book.getRecAge();
        this.imageUrl = book.getImageUrl();
        this.publisher = book.getPublisher();
    }
}
