package com.triple.backend.member.entity;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "member_name")
    private String name;

    private String email;

    private String phone;

    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_code_id")  // 공통 코드 테이블의 소셜 로그인 제공자 참조
    private CommonCode provider;  // 소셜 로그인 제공자 (KAKAO)

    private String providerId;  // 소셜 로그인에서 제공하는 고유 사용자 ID (카카오 ID)
}
