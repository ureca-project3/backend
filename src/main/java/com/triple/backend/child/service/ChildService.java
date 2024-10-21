package com.triple.backend.child.service;

import com.triple.backend.child.dto.ChildInfoResponseDto;

public interface ChildService {
    ChildInfoResponseDto getChildInfo(Long childId);
}