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

    @PostMapping("/analyze/book")
    public ResponseEntity<?> analyzeBook(@RequestBody BookAnalysisRequestDto request) {
        try {
            log.debug("Book Analysis Request - Type: {}", request.getAnalysisType());
            Map<String, Object> result = chatGptService.analyzeBook(
                    request.getContent(),
                    request.getAnalysisType()
            );

            String content = extractContent(result);
            log.info("analyzeBook 내용: {}", content);

            if ("MBTI".equals(request.getAnalysisType())) {
                Map<String, Integer> scores = objectMapper.readValue(content,
                        new TypeReference<Map<String, Integer>>() {});

                MbtiAnalysisDto mbtiAnalysis = MbtiAnalysisDto.builder()
                        .eiScore(scores.get("EI"))
                        .snScore(scores.get("SN"))
                        .tfScore(scores.get("TF"))
                        .jpScore(scores.get("JP"))
                        .build();
                log.info("analyzeBook MBTI 점수: {}", scores);
                log.info("analyzeBook MBTI 분석: {}", mbtiAnalysis);

                Map<String, Object> response = new HashMap<>();
                response.put("mbtiScores", scores);
                response.put("mbtiType", mbtiAnalysis.getMbtiType());

                Map<String, Object> data = new HashMap<>();
                data.put("mbtiScores", scores);
                data.put("mbtiType", mbtiAnalysis.getMbtiType());
                return ResponseEntity.ok(data);
            } else {
                Map<String, String> summary = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});
                return ResponseEntity.ok(summary.get("summary"));
            }
        } catch (Exception e) {
            log.error("책 분석 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("책 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // MBTI 분석 DTO 생성 메서드 추가
    private MbtiAnalysisDto createMbtiAnalysis(Map<String, Integer> scores) {
        return MbtiAnalysisDto.builder()
                .eiScore(Optional.ofNullable(scores.get("E-I")).orElse(50))
                .snScore(Optional.ofNullable(scores.get("S-N")).orElse(50))
                .tfScore(Optional.ofNullable(scores.get("T-F")).orElse(50))
                .jpScore(Optional.ofNullable(scores.get("J-P")).orElse(50))
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("컨트롤러 오류: ", e);
        return CommonResponse.ok("오류가 발생했습니다: " + e.getMessage());
    }

    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = Optional.ofNullable((List<Map<String, Object>>) response.get("choices"))
                    .orElseThrow(() -> new RuntimeException("GPT 응답이 비어있습니다"));
            Map<String, Object> message = Optional.ofNullable((Map<String, Object>) choices.get(0).get("message"))
                    .orElseThrow(() -> new RuntimeException("GPT 응답에 메시지가 없습니다"));
            String content = Optional.ofNullable((String) message.get("content"))
                    .orElseThrow(() -> new RuntimeException("GPT 메시지에 내용이 없습니다"));
            return content;
        } catch (Exception e) {
            log.error("Failed to extract content from response", e);
            throw new RuntimeException("GPT 응답 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}