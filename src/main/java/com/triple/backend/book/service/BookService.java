package com.triple.backend.book.service;

import com.triple.backend.book.dto.BookRankingResponseDto;
import com.triple.backend.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.triple.backend.book.dto.BookDetailResponseDto;
import com.triple.backend.book.dto.BookResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface BookService {

	// 도서 정보 상세 조회
	BookDetailResponseDto getBookDetail(Long bookId);

	// 도서 검색
	Page<BookResponseDto>  getBookSearch(String keyword, Pageable pageable);

	// 좋아요 Top 10 도서 조회
	List<BookRankingResponseDto> getTopLikedBooks();

	// 최신순 도서 목록 조회
	List<BookRankingResponseDto> getBookList(Pageable pageable);

	// 데이터 배치 기능 
	@Transactional(readOnly = false)
	void processExistingBooks();

	// 책 100권 MBTI, 요약
	Map<String, Object> analyzeMbti(Book book);

}
