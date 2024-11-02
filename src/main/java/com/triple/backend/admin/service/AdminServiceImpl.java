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

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            // 엔티티 조회
            Long testId = 1L;
            List<Trait> traits = traitRepository.findByTest(
                    testRepository.findById(testId).orElseThrow(() -> NotFoundException.entityNotFound("책 성향 히스토리 오류"))
            );

            // ChatGPT 응답 분석:
            List<Map<String, Object>> choices = (List<Map<String, Object>>) chatGptResponse.get("choices");
            String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
            try {
                // JSON 파싱 및 MBTI 점수 추출
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Integer> mbtiScores = objectMapper.readValue(content, new TypeReference<Map<String, Integer>>() {
                });

                // BookTraits 생성
                traits.forEach(trait -> {
                    String mbtiKey = switch (trait.getTraitId().intValue()) {
                        case 1 -> "E-I";
                        case 2 -> "S-N";
                        case 3 -> "T-F";
                        case 4 -> "J-P";
                        default -> "E-I"; // 기본값 설정
                    };

                    BookTraits bookTrait = BookTraits.builder()
                            .book(book)
                            .trait(trait)
                            .traitScore(mbtiScores.get(mbtiKey))
                            .build();

                    bookTraitsRepository.save(bookTrait);
                });
            } catch (IOException e) {
                System.out.println("책 성향 오류");
            }
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
