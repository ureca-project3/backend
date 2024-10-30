package com.triple.backend.auth.dto;

import com.triple.backend.common.code.CommonCodeId;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JoinDto {
    private String memberName;
    private String password;
    private String email;
    private String phone;
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime modifiedAt; // 수정 시간
    private CommonCodeId roleCode;    // 공통코드의 codeId (예: "010" 또는 "020")
}
