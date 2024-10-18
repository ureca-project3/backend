package com.triple.backend.login.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
