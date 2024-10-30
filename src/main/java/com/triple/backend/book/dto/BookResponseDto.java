package com.triple.backend.book.dto;

import com.triple.backend.book.entity.Book;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookResponseDto {
	private String title;
	private String recAge;
	private String publisher;
	private String imageUrl;

	public BookResponseDto(Book book) {
		this.title = book.getTitle();
		this.recAge = book.getRecAge();
		this.publisher = book.getPublisher();
		this.imageUrl = book.getImageUrl();
	}
}
