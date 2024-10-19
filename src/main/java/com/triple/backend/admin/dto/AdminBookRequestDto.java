package com.triple.backend.admin.dto;

import com.triple.backend.book.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminBookRequestDto {
    private String title;
    private String author;
    private String publisher;
    private String recAge;
    private String summary;
    private String imageUrl;
    private String genreName;
    private String publishedAt;

    public static Book toEntity(AdminBookRequestDto adminBookRequestDto, String genreCode) {
        return Book.builder()
                .title(adminBookRequestDto.getTitle())
                .author(adminBookRequestDto.getAuthor())
                .publisher(adminBookRequestDto.getPublisher())
                .recAge(adminBookRequestDto.getRecAge())
                .summary(adminBookRequestDto.getSummary())
                .imageUrl(adminBookRequestDto.getImageUrl())
                .genreCode(genreCode)
                .publishedAt(adminBookRequestDto.getPublishedAt())
                .build();
    }
}
