package com.triple.backend.book.controller;


import com.triple.backend.book.entity.Book;
import com.triple.backend.book.entity.BookTraits;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.book.service.BookService;
import com.triple.backend.chatgpt.service.ChatGptService;
import com.triple.backend.test.entity.Trait;
import com.triple.backend.test.repository.TraitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookBatchProcessor {

    private final ChatGptService chatGptService;
    private final BookRepository bookRepository;
    private final BookService bookService; // BookService를 주입

    @Async
    public void processExistingBooks() {
        // 데이터베이스에 이미 존재하는 책 데이터 조회
        List<Book> existingBooks = bookRepository.findAll();

        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // 기존 책 데이터 일괄 처리
        List<CompletableFuture<Void>> futures = existingBooks.stream()
                .map(this::processBook)
                .toList();

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 스레드 풀 종료
        executorService.shutdown();
    }

    private CompletableFuture<Void> processBook(Book book) {
        return CompletableFuture.runAsync(() -> {
            // MBTI 분석 (BookService.analyzeMbti 호출)
            bookService.analyzeMbti(book);
        });
    }
}