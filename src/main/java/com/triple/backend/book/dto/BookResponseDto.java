package com.triple.backend.book.dto;

import com.triple.backend.book.entity.Book;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookResponseDto {
	private String title;
	private Integer recAge;
	private String imageUrl;

	public BookResponseDto(Book book) {
		this.title = book.getTitle();
		this.recAge = book.getRecAge();
		this.imageUrl = book.getImageUrl();
	}
}