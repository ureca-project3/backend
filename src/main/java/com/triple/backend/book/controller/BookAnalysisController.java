package com.triple.backend.book.controller;

import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.book.service.BookService;
import com.triple.backend.chatgpt.dto.BookAnalysisRequestDto;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/books/analyze")
@RequiredArgsConstructor
public class BookAnalysisController {

    private final BookService bookService;
    private final BookRepository bookRepository;

    @PostMapping("/analyze/batch")
    public ResponseEntity<?> analyzeExistingBooks() {
        try {
            bookService.processExistingBooks();
            return CommonResponse.ok("Existing books analyzed successfully");
        } catch (Exception e) {
            return CommonResponse.error("Batch analysis error" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/analyze/mbti")
    public ResponseEntity<?> analyzeMbti(@RequestParam Long bookId) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("도서 정보를 찾을 수 없습니다."));
            Map<String, Object> result = bookService.analyzeMbti(book);
            return CommonResponse.ok("MBTI Analysis Success", result);
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return CommonResponse.error("MBTI Analysis Error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}