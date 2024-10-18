package com.triple.backend.book.entity;

import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
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
    
    private Integer recAge;

    private String summary;

    @Column(name = "bookcover_image")
    private String imageUrl;

    private String publishedAt;

}
