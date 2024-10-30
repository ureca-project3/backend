package com.triple.backend.recbook.controller;

import com.triple.backend.book.dto.BookResponseDto;
import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.recbook.service.RecBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommends")
public class RecBookController {

    private final RecBookService recBookService;

    @GetMapping
    public ResponseEntity<?> getRecommendedBooks(@RequestHeader(name = "Child-Id") Long childId) {
        List<BookResponseDto> recBookList = recBookService.getRecommendedBooks(childId);
        return CommonResponse.ok("Get Recommended Books Success", recBookList);
    }
}
