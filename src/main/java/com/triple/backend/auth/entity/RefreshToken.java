package com.triple.backend.auth.entity;

import com.triple.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_tokens_id")
    private Long refreshtoken_id;

    // 어느 유저의 리프레시 토큰인지 알아야 하기 때문에 Member 엔티티와 연관 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "memberId")
    private Member member;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;  // RefreshToken 만료일

    @Builder
    public RefreshToken(String token, Member member, LocalDateTime expiryDate) {
        this.token = token;
        this.member = member;
        this.expiryDate = expiryDate;
    }
}