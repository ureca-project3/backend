package com.triple.backend.member.dto;

import com.triple.backend.child.entity.Child;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MemberInfoDto {
    private String name;
    private String email;
    private String phone;
    private String provider;
    private List<Child> children; // 자녀 정보 목록
}