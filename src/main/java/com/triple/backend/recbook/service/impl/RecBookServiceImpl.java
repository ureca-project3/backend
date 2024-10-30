package com.triple.backend.recbook.service.impl;

import com.triple.backend.book.dto.BookResponseDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.recbook.repository.RecBookRepository;
import com.triple.backend.recbook.service.RecBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecBookServiceImpl implements RecBookService {

    private final RecBookRepository recBookRepository;
    private final BookRepository bookRepository;

    @Override
    public List<BookResponseDto> getRecommendedBooks(Long childId) {
        List<Long> recommendedBooksId = recBookRepository.findRandomBooksByChildId(childId);
        List<Book> recommendedBooks = bookRepository.findAllById(recommendedBooksId);
        List<BookResponseDto> bookResponseDtos = recommendedBooks.stream()
                .map(BookResponseDto::new)
                .toList();
        return bookResponseDtos;
    }
}
