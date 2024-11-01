package com.triple.backend.recbook.controller;

import com.triple.backend.book.dto.BookResponseDto;
import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.recbook.service.RecBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommends")
public class RecBookController {

    private final RecBookService recBookService;

    // 추천책 도서 조회
    @GetMapping
    public ResponseEntity<?> getRecommendedBooks(@RequestHeader(name = "Child-Id") Long childId) {
        List<BookResponseDto> recBookList = recBookService.getRecommendedBooks(childId);
        return CommonResponse.ok("Get Recommended Books Success", recBookList);
    }

    // 추천책 좋아요
    @PostMapping("/likes/{bookId}")
    public ResponseEntity<?> insertRecBookLike(@PathVariable(name = "bookId") Long bookId,
                                               @RequestHeader(name = "Child-Id") Long childId) {
        recBookService.insertRecBookLike(childId, bookId);
        return CommonResponse.created("Insert Recommended Book Like Success");
    }

    // 추천책 좋아요 취소
    @DeleteMapping("/likes/{bookId}")
    public ResponseEntity<?> deleteRecBookLike(@PathVariable(name = "bookId") Long bookId,
                                        @RequestHeader(name = "Child-Id") Long childId) {
        recBookService.deleteRecBookLike(childId, bookId);
        return CommonResponse.ok("Delete Recommended Book Like Success");
    }

    // 추천책 좋아요 상태조회
    @GetMapping("/likes/{bookId}")
    public ResponseEntity<?> getRecBookLikeStatus(@PathVariable(name = "bookId") Long bookId,
                                           @RequestHeader(name = "Child-Id") Long childId) {
        String status = recBookService.getRecBookLikeStatus(childId, bookId);
        return CommonResponse.ok(status);
    }

}
