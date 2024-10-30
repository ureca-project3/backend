package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.JoinDto;

public interface AuthService {
    void joinProcess(JoinDto joinDto);

    // 회원가입시 이메일 중복 체크
    boolean existsByEmail(String email);
}