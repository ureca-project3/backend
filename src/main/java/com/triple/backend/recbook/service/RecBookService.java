package com.triple.backend.recbook.service;

import com.triple.backend.book.dto.BookResponseDto;

import java.util.List;

public interface RecBookService {
    List<BookResponseDto> getRecommendedBooks(Long childId);

}
