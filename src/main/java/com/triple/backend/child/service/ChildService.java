package com.triple.backend.child.service;

import com.triple.backend.child.dto.*;

import java.time.LocalDateTime;

public interface ChildService {
    // 자녀 정보, 최신 히스토리 조회
    ChildInfoResponseDto getChildInfo(Long childId);

    // 자녀 히스토리 조회
    ChildHistoryResponseDto getChildHistory(Long childId, LocalDateTime date);

    // 자녀 결과 조회
    ChildTestHistoryResponseDto getChildTestHistory(Long childId);

    // 자녀 성향 진단 결과 모음 날짜 조회
    ChildTestHistoryDateResponseDto getChildTestHistoryDate(Long childId, LocalDateTime date);

    void registerChild(ChildRegisterRequestDto request, String accessToken);

    boolean deleteMyChild(String accessToken);
}