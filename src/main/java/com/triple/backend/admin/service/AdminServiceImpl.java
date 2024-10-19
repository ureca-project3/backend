package com.triple.backend.admin.service;

import com.triple.backend.admin.dto.AdminBookRequestDto;
import com.triple.backend.admin.dto.AdminBookResponseDto;
import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.entity.Genre;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void insertBook(AdminBookRequestDto adminBookRequestDto) {
        String genreName = adminBookRequestDto.getGenreName();
        Book book = AdminBookRequestDto.toEntity(adminBookRequestDto, Genre.getGenreCode(genreName));
        bookRepository.save(book);
    }

    @Override
    public List<AdminBookResponseDto> getBookList(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        List<AdminBookResponseDto> response = books.stream()
                .map(AdminBookResponseDto::toDto)
                .toList();
        return response;
    }

    @Override
    public AdminBookResponseDto getBookDetail(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> NotFoundException.entityNotFound("책"));
        return AdminBookResponseDto.toDto(book);
    }

    @Override
    @Transactional
    public void updateBook(Long bookId, AdminBookUpdateRequestDto adminBookRequestDto) {
        String genreCode = Genre.getGenreCode(adminBookRequestDto.getGenreName());
        Book book = bookRepository.findById(bookId).orElseThrow(() -> NotFoundException.entityNotFound("책"));
        book.updateBook(adminBookRequestDto, genreCode);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        bookRepository.deleteById(bookId);
    }
}
