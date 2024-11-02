package com.triple.backend.child.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
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
    public Child(Member member, String name, String birthdate, String gender, String imageUrl, Integer age) {
        this.member = member;
        this.name = name;
        this.birthdate = birthdate;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.age = age;
    }

}
