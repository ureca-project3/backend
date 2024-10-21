package com.triple.backend.book.entity;

import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String title;

    private String author;
    
    private String publisher;
    
    private String recAge;

    private String summary;

    private String genreCode;

    @Column(name = "bookcover_image")
    private String imageUrl;

    private String publishedAt;

    @Builder
    public Book (String title, String author, String publisher, String recAge, String summary,
                 String genreCode, String imageUrl, String publishedAt) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.recAge = recAge;
        this.summary = summary;
        this.genreCode = genreCode;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
    }

    public void updateBook (AdminBookUpdateRequestDto dto, String genreCode) {
        this.title = dto.getTitle();
        this.author = dto.getAuthor();
        this.publisher = dto.getPublisher();
        this.recAge = dto.getRecAge();
        this.summary = dto.getSummary();
        this.genreCode = genreCode;
        this.imageUrl = dto.getImageUrl();
        this.publishedAt = dto.getPublishedAt();
    }
}
