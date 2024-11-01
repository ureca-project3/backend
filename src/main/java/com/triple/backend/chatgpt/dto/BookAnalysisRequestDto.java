package com.triple.backend.chatgpt.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 책 분석 결과
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookAnalysisRequestDto {
    private String content;        // 책 내용
    private String analysisType;   // "MBTI" 또는 "SUMMARY"

    @Builder
    public BookAnalysisRequestDto(String content, String analysisType) {
        this.content = content;
        this.analysisType = analysisType;
    }
}
