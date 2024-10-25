package com.triple.backend.test.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Mbti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mbtiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test;

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti_name")
    private MbtiType name;

    @Column(name = "mbti_phrase")
    private String phrase;

    @Column(name = "mbti_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "mbti_image")
    private String image;
}
