package com.triple.backend.member.service;

import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public Member authenticateByEmail(String email, String password) {
        Member member = memberRepository.findByEmail(email);
        if (member != null && member.getPassword().equals(password)) { // 비밀번호 비교는 해싱을 사용해야 합니다.
            return member;
        }
        return null;
    }
}
