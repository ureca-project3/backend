package com.triple.backend.book.service;

import com.triple.backend.book.dto.BookRankingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.triple.backend.book.dto.BookDetailResponseDto;
import com.triple.backend.book.dto.BookResponseDto;

import java.util.List;

public interface BookService {

	// 도서 정보 상세 조회
	BookDetailResponseDto getBookDetail(Long bookId);

	// 도서 검색
	Page<BookResponseDto>  getBookSearch(String keyword, Pageable pageable);

	// 좋아요 Top 10 도서 조회
	List<BookRankingResponseDto> getTopLikedBooks();

	// 최신순 도서 목록 조회
	List<BookResponseDto> getBookList(Pageable pageable);
}
