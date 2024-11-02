package com.triple.backend.admin.service;

import com.triple.backend.admin.dto.AdminBookRequestDto;
import com.triple.backend.admin.dto.AdminBookResponseDto;
import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.entity.BookTraits;
import com.triple.backend.book.entity.Genre;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.chatgpt.dto.ChatCompletionDto;
import com.triple.backend.chatgpt.dto.ChatRequestMsgDto;
import com.triple.backend.chatgpt.service.ChatGptService;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.Trait;
import com.triple.backend.test.repository.TestRepository;
import com.triple.backend.test.repository.TraitRepository;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BookRepository bookRepository;
    private final ChatGptService chatGptService;
    private final TraitRepository traitRepository;
    private final TestRepository testRepository;
    private final BookTraitsRepository bookTraitsRepository;

    @Value("${openai.prompt.system}")
    private String systemPrompt;

    @Override
    @Transactional
    public void insertBook(AdminBookRequestDto adminBookRequestDto) {
        String genreName = adminBookRequestDto.getGenreName();
        Book book = AdminBookRequestDto.toEntity(adminBookRequestDto, Genre.getGenreCode(genreName));
        bookRepository.save(book);

        // ChatGPT로 요약 보내기
        String summary = adminBookRequestDto.getSummary();

        List<ChatRequestMsgDto> chatRequestMsgDtos = new ArrayList<>();
        // 시스템 메시지로 목적 설명
        ChatRequestMsgDto systemMessage = ChatRequestMsgDto.builder()
                .role("system")
                .content(systemPrompt)
                .build();

        // 책 요약을 content로 전송
        ChatRequestMsgDto userMessage = ChatRequestMsgDto.builder()
                .role("user")
                .content(summary)
                .build();
        chatRequestMsgDtos.add(systemMessage);
        chatRequestMsgDtos.add(userMessage);

        ChatCompletionDto chatCompletionDto = ChatCompletionDto.builder()
                .messages(chatRequestMsgDtos)
                .build();


        Map<String, Object> chatGptResponse = chatGptService.selectPrompt(chatCompletionDto);
        if (chatGptResponse != null) {
            // ChatGPT 응답 출력
            System.out.println("ChatGPT 응답: " + chatGptResponse);
            Long testId = 1L;
            Test test = testRepository.findById(testId).orElseThrow(() -> NotFoundException.entityNotFound("테스트")); // testId 에 해당하는 Test 엔티티를 조회
            List<Trait> traits = traitRepository.findByTest(test);

            // ChatGPT 응답을 JSON 객체로 파싱
            JSONObject jsonObject = new JSONObject(chatGptResponse);

            // 각 trait에 대해 BookTraits 생성 및 저장
            traits.forEach(trait -> {
                // traitId를 사용하여 jsonObject에서 점수 가져오기
                Integer traitScore = Integer.parseInt(jsonObject.get(trait.getTraitId().toString()).toString());

                BookTraits bookTrait = BookTraits.builder()
                        .book(book)
                        .trait(trait)
                        .traitScore(traitScore)
                        .build();

                bookTraitsRepository.save(bookTrait);
            });
        }
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
