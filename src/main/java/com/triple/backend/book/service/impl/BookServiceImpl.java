package com.triple.backend.book.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.triple.backend.book.dto.BookDetailResponseDto;
import com.triple.backend.book.dto.BookResponseDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.book.service.BookService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;

	/**
	 *	도서 정보 상세 조회
	 * 	- TODO: 회원인증필요
	 */
	@Override
	public BookDetailResponseDto getBookDetail(Long bookId) {
		Book book = bookRepository.findById(bookId)
			.orElseThrow(() -> new IllegalArgumentException("도서 정보를 찾을 수 없습니다."));

		return new BookDetailResponseDto(book);
	}

	/**
	 * 도서 검색
	 */
	@Override
	public Page<BookResponseDto> getBookSearch(String keyword, Pageable pageable) {
		Page<Book> books = bookRepository.searchBookByKeyword(keyword, pageable);

		return books.map(BookResponseDto::new);
	}
}
