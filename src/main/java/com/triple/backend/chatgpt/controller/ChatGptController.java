package com.triple.backend.chatgpt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.chatgpt.dto.BookAnalysisRequestDto;
import com.triple.backend.chatgpt.dto.ChatCompletionDto;
import com.triple.backend.chatgpt.dto.CompletionDto;
import com.triple.backend.chatgpt.dto.MbtiAnalysisDto;
import com.triple.backend.chatgpt.service.ChatGptService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/gpt")
@RequiredArgsConstructor
public class ChatGptController {
    private final ChatGptService chatGptService;
    private final ObjectMapper objectMapper;

    // chat gpt의 모델 리스트를 조회
    @GetMapping("/modelList")
    public ResponseEntity<?> selectModelList() {
        List<Map<String, Object>> result = chatGptService.selectModelList();
        return CommonResponse.ok("Select Model List Success", result);
    }

    // chat gpt의 유효한 모델을 조회
    @GetMapping("/model")
    public ResponseEntity<?> isValidModel(@RequestParam(name = "modelName") String modelName) {
        Map<String, Object> result = chatGptService.isValidModel(modelName);
        return CommonResponse.ok("Is Valid Model Success", result);
    }

    // legacy chat gpt 프롬프트 명령을 수행
    @PostMapping("/legacyPrompt")
    public ResponseEntity<?> selectLegacyPrompt(
            @RequestBody CompletionDto completionDto) {
        log.debug("param :: " + completionDto.toString());
        Map<String, Object> result = chatGptService.selectLegacyPrompt(completionDto);
        return CommonResponse.ok("Select Legacy Prompt Success", result);
    }

    // 최신 chat gpt 프롬프트 명령을 수행
    @PostMapping("/prompt")
    public ResponseEntity<?> selectPrompt(
            @RequestBody ChatCompletionDto completionDto) {
        log.debug("param :: " + completionDto.toString());
        Map<String, Object> result = chatGptService.selectPrompt(completionDto);
        return CommonResponse.ok("Select Prompt Success", result);
    }


    @PostMapping("/analyze/mbti")
    public ResponseEntity<?> analyzeMbti(@RequestBody BookAnalysisRequestDto request) {
        try {
            Map<String, Object> result = chatGptService.analyzeMbti(request);
            return CommonResponse.ok("MBTI Analysis Success", result);
        } catch (Exception e) {
            log.error("MBTI 분석 중 오류 발생: ", e);
            return CommonResponse.error("MBTI 분석 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/analyze/summary")
    public ResponseEntity<?> analyzeSummary(@RequestBody BookAnalysisRequestDto request) {
        try {
            Map<String, String> result = chatGptService.analyzeSummary(request);
            return CommonResponse.ok("Book Summary", result.get("summary"));
        } catch (Exception e) {
            log.error("책 요약 중 오류 발생: ", e);
            return CommonResponse.error("책 요약 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("컨트롤러 오류: ", e);
        return CommonResponse.ok("오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}