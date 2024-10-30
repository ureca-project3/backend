package com.triple.backend.child.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long childId;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "child_name")
    private String name;

    private String gender;

    private String birthdate;

    @Column(name = "profile_image")
    private String imageUrl;

    private Integer age;

}
