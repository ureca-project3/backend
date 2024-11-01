package com.triple.backend.chatgpt.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 책 내용을 분석하여 도출된 MBTI 유형 분석 정보
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MbtiAnalysisDto {
    private int eiScore;  // Extraversion-Introversion
    private int snScore;  // Sensing-Intuition
    private int tfScore;  // Thinking-Feeling
    private int jpScore;  // Judging-Perceiving

    @Builder
    public MbtiAnalysisDto(int eiScore, int snScore, int tfScore, int jpScore) {
        this.eiScore = eiScore;
        this.snScore = snScore;
        this.tfScore = tfScore;
        this.jpScore = jpScore;
    }

    public String getMbtiType() {
        StringBuilder mbti = new StringBuilder();
        mbti.append(eiScore >= 50 ? "I" : "E");
        mbti.append(snScore >= 50 ? "N" : "S");
        mbti.append(tfScore >= 50 ? "F" : "T");
        mbti.append(jpScore >= 50 ? "P" : "J");
        return mbti.toString();
    }
}