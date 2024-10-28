package com.triple.backend.auth.dto;

import com.triple.backend.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// 사용자 인증 및 권한 부여
public class CustomMemberDetails implements UserDetails {
    private final Member member;

    public CustomMemberDetails(Member member) {
        this.member = member;

    }

    // 사용자의 role 값을 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // 역할을 문자열로 반환하도록 수정
        String role = member.getRole_code(); // 역할이 문자열이어야 함
        authorities.add(new SimpleGrantedAuthority(role));

        return authorities;
    }

    // password 값을 반환
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // 사용자 이름 반환
    @Override
    public String getUsername() {
        return member.getName();
    }


    public String getUserPhone() {
        return member.getPhone();
    }


    public String getUseremail() {
        return member.getEmail();
    }

    // Member 엔티티에서 getMemberId를 반환
    public Long getMemberId() {
        return member.getMemberId();
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겨 있는지 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }

    public Member getMember() {
        return member;
    }
}