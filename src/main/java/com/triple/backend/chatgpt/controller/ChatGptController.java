package com.triple.backend.chatgpt.controller;

import com.triple.backend.chatgpt.dto.ChatCompletionDto;
import com.triple.backend.chatgpt.dto.CompletionDto;
import com.triple.backend.chatgpt.service.ChatGptService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/gpt")
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatGptService chatGptService;

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
}
