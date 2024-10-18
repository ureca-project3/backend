package com.triple.backend.login.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class LoginDto {
    private final String message;
    private final Data data;
    private final String timestamp;

    @Getter
    @AllArgsConstructor
    public static class Data {
        private final String accessToken;
        private final String refreshToken;
    }
}