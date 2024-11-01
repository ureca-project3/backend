package com.triple.backend.book.controller;

import com.triple.backend.book.entity.Book;
import com.triple.backend.book.service.BookService;
import com.triple.backend.chatgpt.dto.BookAnalysisRequestDto;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/books/analyze")
@RequiredArgsConstructor
public class BookAnalysisController {

    private final BookBatchProcessor bookBatchProcessor;
    private final BookService bookService;

    @PostMapping("/analyze/batch")
    public ResponseEntity<?> analyzeExistingBooks() {
        try {
            bookBatchProcessor.processExistingBooks();
            return CommonResponse.ok("Existing books analyzed successfully");
        } catch (Exception e) {
            return CommonResponse.error("Batch analysis error" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/analyze/mbti")
    public ResponseEntity<?> analyzeMbti(@RequestBody Book book) {
        try {
            Map<String, Object> result = bookService.analyzeMbti(book);
            return CommonResponse.ok("MBTI Analysis Success", result);
        } catch (Exception e) {
            return CommonResponse.error("MBTI Analysis Error"+ e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}