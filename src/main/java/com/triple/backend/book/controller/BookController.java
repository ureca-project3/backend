package com.triple.backend.book.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.triple.backend.book.service.BookService;
import com.triple.backend.common.dto.CommonResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

	private final BookService bookService;

	/**
	 *	도서 정보 상세 조회
	 * 	- TODO: 회원인증필요
	 */
	@GetMapping("/{bookId}")
	public ResponseEntity<?> getBookDetail(@PathVariable(name = "bookId") Long bookId) {

		return CommonResponse.ok(
			"Get BookDetail Success",
			bookService.getBookDetail(bookId)
		);
	}

	/**
	 * 	도서 검색
	 */
	@GetMapping("/search")
	public ResponseEntity<?> getBookSearch(
		@RequestParam(value = "keyword") String keyword,
		@PageableDefault(page = 0, size = 10) Pageable pageable
	) {
		return CommonResponse.ok(
			"Get BookSearch Success",
			bookService.getBookSearch(keyword, pageable)
		);
	}
}