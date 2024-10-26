package com.triple.backend.child.service;

import com.triple.backend.child.dto.ChildRegisterRequestDto;

public interface ChildService {
    void registerChild(ChildRegisterRequestDto request, String accessToken);
}
