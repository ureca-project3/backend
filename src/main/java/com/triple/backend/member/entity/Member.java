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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumns({
//            @JoinColumn(name = "provider_code_id", referencedColumnName = "codeId"),
//            @JoinColumn(name = "provider_group_id", referencedColumnName = "groupId")
//    })
//    // 공통 코드 테이블의 소셜 로그인 제공자 참조
//    private CommonCode provider;  // 소셜 로그인 제공자 (KAKAO)

    private String provider;  // 소셜 로그인 제공자 (KAKAO) 일단 string으로 구현
    private String providerId;  // 소셜 로그인에서 제공하는 고유 사용자 ID (카카오 ID)

    @Builder
    public Member(String providerId, String provider, String name, String email, String phone) {
        this.providerId = providerId;
        this.provider = provider;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Member() {

    }
    // 역할을 부여하기 위해 CommonCode 참조 추가
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "role_code_id", referencedColumnName = "codeId"),
            @JoinColumn(name = "role_group_id", referencedColumnName = "groupId")
    })
    private CommonCode role;
}