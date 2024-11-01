package com.triple.backend.chatgpt.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.chatgpt.config.ChatGptConfig;
import com.triple.backend.chatgpt.dto.ChatCompletionDto;
import com.triple.backend.chatgpt.dto.ChatRequestMsgDto;
import com.triple.backend.chatgpt.dto.CompletionDto;
import com.triple.backend.chatgpt.dto.MbtiAnalysisDto;
import com.triple.backend.chatgpt.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGptServiceImpl implements ChatGptService {

    private final ChatGptConfig chatGptConfig;

    @Value("${openai.url.model}")
    private String modelUrl;

    @Value("${openai.url.model-list}")
    private String modelListUrl;

    @Value("${openai.url.prompt}")
    private String promptUrl;

    @Value("${openai.url.legacy-prompt}")
    private String legacyPromptUrl;

    @Override
    public List<Map<String, Object>> selectModelList() {
        log.debug("[+] 모델 리스트를 조회합니다.");
        List<Map<String, Object>> resultList = null;

        HttpHeaders headers = chatGptConfig.httpHeaders();
        ResponseEntity<String> response = chatGptConfig
                .restTemplate()
                .exchange(modelUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        try {
            // [STEP3] Jackson을 기반으로 응답값을 가져옵니다.
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> data = om.readValue(response.getBody(), new TypeReference<>() {
            });

            // [STEP4] 응답 값을 결과값에 넣고 출력을 해봅니다.
            resultList = (List<Map<String, Object>>) data.get("data");
            for (Map<String, Object> object : resultList) {
                log.debug("ID: " + object.get("id"));
                log.debug("Object: " + object.get("object"));
                log.debug("Created: " + object.get("created"));
                log.debug("Owned By: " + object.get("owned_by"));
            }
        } catch (JsonMappingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.debug("JsonProcessingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }
        return resultList;
    }

    @Override
    public Map<String, Object> isValidModel(String modelName) {
        log.debug("[+] 모델이 유효한지 조회합니다. 모델 : " + modelName);
        Map<String, Object> result = new HashMap<>();

        HttpHeaders headers = chatGptConfig.httpHeaders();
        ResponseEntity<String> response = chatGptConfig
                .restTemplate()
                .exchange(modelListUrl + "/" + modelName, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        try {
            // [STEP3] Jackson을 기반으로 응답값을 가져옵니다.
            ObjectMapper om = new ObjectMapper();
            result = om.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> selectLegacyPrompt(CompletionDto completionDto) {
        log.debug("[+] 레거시 프롬프트를 수행합니다.");

        HttpHeaders headers = chatGptConfig.httpHeaders();
        HttpEntity<CompletionDto> requestEntity = new HttpEntity<>(completionDto, headers);
        ResponseEntity<String> response = chatGptConfig
                .restTemplate()
                .exchange(legacyPromptUrl, HttpMethod.POST, requestEntity, String.class);

        Map<String, Object> resultMap = new HashMap<>();
        try {
            ObjectMapper om = new ObjectMapper();
            resultMap = om.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }
        return resultMap;
    }

    //
    @Override
    public Map<String, Object> selectPrompt(ChatCompletionDto chatCompletionDto) {
        log.debug("[+] 신규 프롬프트를 수행합니다.");

        Map<String, Object> resultMap = new HashMap<>();

        HttpHeaders headers = chatGptConfig.httpHeaders();
        HttpEntity<ChatCompletionDto> requestEntity = new HttpEntity<>(chatCompletionDto, headers);
        ResponseEntity<String> response = chatGptConfig
                .restTemplate()
                .exchange(promptUrl, HttpMethod.POST, requestEntity, String.class);
        try {
            ObjectMapper om = new ObjectMapper();
            resultMap = om.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> analyzeBook(String content, String analysisType) {
        List<ChatRequestMsgDto> messages = new ArrayList<>();

        // 분석 유형에 따른 프롬프트 설정
        String systemPrompt = getSystemPrompt(analysisType);

        messages.add(ChatRequestMsgDto.builder()
                .role("system")
                .content(systemPrompt)
                .build());

        messages.add(ChatRequestMsgDto.builder()
                .role("user")
                .content(content)
                .build());

        ChatCompletionDto chatCompletionDto = ChatCompletionDto.builder()
                .messages(messages)
                .build();  // gpt-4o-mini 모델 사용

        return selectPrompt(chatCompletionDto);
    }

    private String getSystemPrompt(String analysisType) {
        if ("MBTI".equals(analysisType)) {
            return "책의 내용을 기반으로 MBTI 성향을 분석하세요. " +
                    "다음 형식의 JSON으로만 응답하세요: " +
                    "{\"EI\": score, \"SN\": score, \"TF\": score, \"JP\": score} " +
                    "각 점수는 0에서 100 사이의 정수여야 합니다. " +
                    "절대로 JSON 형식 이외의 어떤 텍스트도 포함하지 마세요. " +
                    "JSON 형식에 어긋나는 응답은 처리할 수 없습니다.";
        } else {
            return "책의 내용을 300자 이내로 간단히 요약하세요. " +
                    "결과를 JSON 형식으로 다음과 같이 반환하세요: " +
                    "{\"summary\": \"요약 내용\"} " +
                    "절대로 JSON 형식 이외의 어떤 텍스트도 포함하지 마세요. " +
                    "JSON 형식에 어긋나는 응답은 처리할 수 없습니다.";
        }
    }
}

