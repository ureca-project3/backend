package com.triple.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminBookUpdateRequestDto {
    private String title;
    private String author;
    private String publisher;
    private String recAge;
    private String summary;
    private String imageUrl;
    private String genreName;
    private String publishedAt;
}
