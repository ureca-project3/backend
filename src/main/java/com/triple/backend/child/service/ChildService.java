package com.triple.backend.child.service;

import com.triple.backend.child.dto.ChildHistoryResponseDto;
import com.triple.backend.child.dto.ChildInfoResponseDto;
import com.triple.backend.child.dto.ChildTestHistoryDateResponseDto;
import com.triple.backend.child.dto.ChildTestHistoryResponseDto;

public interface ChildService {
    // 자녀 정보, 최신 히스토리 조회
    ChildInfoResponseDto getChildInfo(Long childId);

    // 자녀 히스토리 조회
    ChildHistoryResponseDto getChildHistory(Long childId, String date);

    // 자녀 결과 조회
    ChildTestHistoryResponseDto getChildTestHistory(Long childId);

    // 자녀 성향 진단 결과 모음 날짜 조회
    ChildTestHistoryDateResponseDto getChildTestHistoryDate(Long childId, String date);
}