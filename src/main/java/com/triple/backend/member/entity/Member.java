package com.triple.backend.member.entity;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
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

    // 역할을 부여하기 위해 CommonCode 참조 추가
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "role_code_id", referencedColumnName = "codeId"),
            @JoinColumn(name = "role_group_id", referencedColumnName = "groupId")
    })
    private CommonCode role;
}
