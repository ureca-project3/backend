package com.triple.backend.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberInfoDto {
    private String name;
    private String email;
    private String phone;
}