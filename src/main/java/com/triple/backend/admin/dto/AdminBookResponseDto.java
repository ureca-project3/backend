package com.triple.backend.admin.dto;

import com.triple.backend.book.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminBookResponseDto {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String recAge;
    private String summary;
    private String imageUrl;
    private String genreName;
    private String publishedAt;

    public static AdminBookResponseDto toDto(Book book) {
        return new AdminBookResponseDto(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getRecAge(),
                book.getSummary(),
                book.getImageUrl(),
                book.getGenreCode(),
                book.getPublishedAt()
        );
    }
}
