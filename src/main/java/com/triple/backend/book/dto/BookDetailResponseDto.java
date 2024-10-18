package com.triple.backend.book.dto;

import com.triple.backend.book.entity.Book;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookDetailResponseDto {
	private String title;
	private String author;
	private String publisher;
	private Integer recAge;
	private String summary;
	private String imageUrl;
	private String publishedAt;

	public BookDetailResponseDto(Book book) {
		this.title = book.getTitle();
		this.author = book.getAuthor();
		this.publisher = book.getPublisher();
		this.recAge = book.getRecAge();
		this.summary = book.getSummary();
		this.imageUrl = book.getImageUrl();
		this.publishedAt = book.getPublishedAt();
	}
}
