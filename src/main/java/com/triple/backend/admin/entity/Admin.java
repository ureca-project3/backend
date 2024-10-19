package com.triple.backend.admin.entity;

import com.triple.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(name = "admin_name")
    private String name;

    @Column(name = "admin_email")
    private String email;

    @Column(name = "admin_password")
    private String password;
}
