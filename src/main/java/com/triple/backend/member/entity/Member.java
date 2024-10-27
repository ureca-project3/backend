package com.triple.backend.member.entity;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "member_name")
    private String name;

    private String email;

    private String phone;

    private String password;

    private String provider;  // 소셜 로그인 제공자 (KAKAO) 일단 string으로 구현
    private String providerId;  // 소셜 로그인에서 제공하는 고유 사용자 ID (카카오 ID)
    private String role;

    // 생성자에 @Builder 어노테이션을 적용하여 소셜 로그인과 일반 로그인을 구분하는 방식
    @Builder
    public Member(String name, String email, String phone, String password, String provider, String providerId,String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    @Builder

    public Member(String providerId, String provider, String name, String email, String phone,String role) {
        this.providerId = providerId;
        this.provider = provider;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role; // 역할 필드 초기화
    }

    public Member() {

    }

}
