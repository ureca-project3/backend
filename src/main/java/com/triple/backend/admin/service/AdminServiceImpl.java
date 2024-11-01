package com.triple.backend.admin.service;

import com.triple.backend.admin.dto.AdminBookRequestDto;
import com.triple.backend.admin.dto.AdminBookResponseDto;
import com.triple.backend.admin.dto.AdminBookUpdateRequestDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.entity.Genre;
import com.triple.backend.book.repository.BookRepository;
import com.triple.backend.chatgpt.dto.ChatCompletionDto;
import com.triple.backend.chatgpt.dto.ChatRequestMsgDto;
import com.triple.backend.chatgpt.service.ChatGptService;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.Trait;
import com.triple.backend.test.repository.TestRepository;
import com.triple.backend.test.repository.TraitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BookRepository bookRepository;
    private final ChatGptService chatGptService;
    private final TraitRepository traitRepository;
    private final TestRepository testRepository;

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

        // 응답을 바탕으로 bookTraits 업데이트 (미완성/ gpt의 응답을 잘 쪼개서 bookTrait에 차곡차곡 들어가도록 해야 함)
        if (chatGptResponse != null) {
            Long testId = 1L;
            Test test = testRepository.findById(testId).orElseThrow(() -> NotFoundException.entityNotFound("테스트"));
            List<Trait> traits = traitRepository.findByTest(test);
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
