package com.triple.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDto {
    private String memberName;
    private String password;
    private String email;
    private String phone;
}
