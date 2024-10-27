package com.triple.backend.member.service;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.entity.MemberInfoDto;
import com.triple.backend.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {
    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;

    // 데이터베이스에서 특정 이름 조회 , DB 로그인 기능 구현을 위함
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username);

        if(member != null){
            return new CustomMemberDetails(member);
        }
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username); // 사용자 없을 경우 예외 처리
    }

    // 특정 이메일로 사용자 조회
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // 회원과 자녀 정보를 포함하는 UserProfileDto 조회
    public MemberInfoDto getUserProfileById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Child> children = childRepository.findAllByMember(member);

        // UserProfileDto로 변환하여 반환
        return new MemberInfoDto(
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                children
        );
    }

    // memberId를 기반으로 provider 정보 조회 - 카카오 계정 로그아웃 시 사용
    public String getProviderByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return member.getProvider(); // provider 정보 반환
    }
}
