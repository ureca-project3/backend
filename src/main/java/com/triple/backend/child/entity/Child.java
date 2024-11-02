package com.triple.backend.child.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    @Builder
    public Child(Long childId, String name, String gender, String birthdate, String imageUrl, Integer age) {
        this.childId = childId;
        this.name = name;
        this.gender = gender;
        this.birthdate = birthdate;
        this.imageUrl = imageUrl;
        this.age = age;
    }
}
