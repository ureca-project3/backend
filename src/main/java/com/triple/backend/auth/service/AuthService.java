package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.JoinDto;

public interface AuthService {
    void joinProcess(JoinDto joinDto);
}