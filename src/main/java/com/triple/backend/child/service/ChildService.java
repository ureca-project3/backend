package com.triple.backend.child.service;

import com.triple.backend.child.dto.ChildHistoryResponseDto;
import com.triple.backend.child.dto.ChildInfoResponseDto;
import com.triple.backend.child.dto.ChildTestHistoryResponseDto;
import org.aspectj.weaver.bcel.UnwovenClassFile;

public interface ChildService {
    // 자녀 정보, 최신 히스토리 조회
    ChildInfoResponseDto getChildInfo(Long childId);

    // 자녀 히스토리 조회
    ChildHistoryResponseDto getChildHistory(Long childId, String date);

    // 자녀 결과 조회
    ChildTestHistoryResponseDto getChildTestHistory(Long childId);
}