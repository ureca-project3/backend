package com.triple.backend.recbook.service.impl;

import com.triple.backend.book.dto.BookResponseDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.recbook.entity.RecBook;
import com.triple.backend.recbook.entity.RecBookLike;
import com.triple.backend.recbook.repository.RecBookLikeRepository;
import com.triple.backend.recbook.repository.RecBookRepository;
import com.triple.backend.recbook.service.RecBookService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecBookServiceImpl implements RecBookService {

    private final RecBookRepository recBookRepository;
    private final RecBookLikeRepository recBookLikeRepository;
    private final BookRepository bookRepository;
    private final ChildRepository childRepository;

    @Override
    public List<BookResponseDto> getRecommendedBooks(Long childId) {
        List<Long> recommendedBooksId = recBookRepository.findRandomBooksByChildId(childId);
        List<Book> recommendedBooks = bookRepository.findAllById(recommendedBooksId);
        List<BookResponseDto> bookResponseDtos = recommendedBooks.stream()
                .map(BookResponseDto::new)
                .toList();
        return bookResponseDtos;
    }

    @Override
    @Transactional
    public void insertRecBookLike(Long childId, Long bookId) {
        RecBook recBook = recBookRepository.findByChildIdAndBookId(childId, bookId).orElseThrow(
                () -> NotFoundException.entityNotFound("추천책"));
        Child child = childRepository.findById(childId).orElseThrow(
                () -> NotFoundException.entityNotFound("아이"));

        RecBookLike recBookLike = RecBookLike.builder()
                .child(child)
                .recBook(recBook)
                .build();
        recBookLikeRepository.save(recBookLike);
    }

    @Override
    @Transactional
    public void deleteRecBookLike(Long childId, Long bookId) {
        RecBook recBook = recBookRepository.findByChildIdAndBookId(childId, bookId).orElseThrow(
                () -> NotFoundException.entityNotFound("추천책"));
        Long recBookLikeId = recBookLikeRepository.findByRecBookIdAndChildId(recBook.getRecBookId(), childId);
        if (recBookLikeId == null) {
            throw NotFoundException.entityNotFound("추천책 좋아요");
        }
        recBookLikeRepository.deleteById(recBookLikeId);
    }

    @Override
    public String getRecBookLikeStatus(Long childId, Long bookId) {
        RecBook recBook = recBookRepository.findByChildIdAndBookId(childId, bookId).orElseThrow(
                () -> NotFoundException.entityNotFound("추천책"));
        Long recBookLikeId = recBookLikeRepository.findByRecBookIdAndChildId(recBook.getRecBookId(), childId);
        if (recBookLikeId == null) {
            return "좋아요하지 않은 추천책입니다.";
        }
        return "좋아요한 추천책입니다";
    }
}
