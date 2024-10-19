package com.triple.backend.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.triple.backend.book.dto.BookDetailResponseDto;
import com.triple.backend.book.dto.BookResponseDto;

public interface BookService {

	// 도서 정보 상세 조회
	BookDetailResponseDto getBookDetail(Long bookId);

	// 도서 검색
	Page<BookResponseDto>  getBookSearch(String keyword, Pageable pageable);
}
