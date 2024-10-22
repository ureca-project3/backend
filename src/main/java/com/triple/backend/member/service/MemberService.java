package com.triple.backend.member.service;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // 이 클래스가 서비스 컴포넌트임을 명시
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 데이터베이스에서 특정 이름 조회 , DB 로그인 기능 구현을 위함
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username);

        if (member != null) {
            return new CustomMemberDetails(member);
        }

        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username); // 사용자 없을 경우 예외 처리
    }

    // 특정 이메일로 사용자 조회
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
