package com.triple.backend.book.service;

import com.triple.backend.book.dto.BookDetailResponseDto;

public interface BookService {

	// 도서 정보 상세 조회
	BookDetailResponseDto getBookDetail(Long bookId);
}
