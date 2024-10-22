package com.triple.backend.child.service;

import com.triple.backend.child.dto.ChildHistoryResponseDto;
import com.triple.backend.child.dto.ChildInfoResponseDto;
import org.aspectj.weaver.bcel.UnwovenClassFile;

public interface ChildService {
    ChildInfoResponseDto getChildInfo(Long childId);

    // 자녀 히스토리 조회
    ChildHistoryResponseDto getChildHistory(Long childId, String date);
}