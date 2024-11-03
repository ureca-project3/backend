package com.triple.backend.member.service.impl;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.child.dto.ChildDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.member.dto.MemberUpdateDto;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.dto.MemberInfoDto;
import com.triple.backend.member.repository.MemberRepository;
import com.triple.backend.member.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {
    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username);

        if (member != null) {
            return new CustomMemberDetails(member);
        }
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public MemberInfoDto getUserProfileById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Child> children = childRepository.findAllByMember(member);

        return new MemberInfoDto(
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getProvider(),
                children
        );
    }

    public String getProviderByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return member.getProvider();
    }


    @Override
    public List<ChildDto> getChildrenByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Child> children = childRepository.findAllByMember(member);

        return children.stream()
                .map(ChildDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEmailDuplicate(String email, Long memberId) {
        return memberRepository.existsByEmailAndMemberIdNot(email, memberId);
    }

    @Override
    public void updateMemberInfo(Long memberId, MemberUpdateDto memberUpdateDto) {
        // 기존 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 카카오 로그인 사용자 체크
        if ("kakao".equalsIgnoreCase(member.getProvider())) {
            throw new IllegalStateException("카카오 로그인 사용자는 정보를 수정할 수 없습니다.");
        }
        // 필드 업데이트 전에 이메일 중복 체크
        if (!member.getEmail().equals(memberUpdateDto.getEmail()) &&
                memberRepository.existsByEmailAndMemberIdNot(memberUpdateDto.getEmail(), memberId)) {
            throw new IllegalStateException("이메일이 이미 존재합니다");
        }

        // 필드 업데이트
        String encodedPassword = "";
        if (memberUpdateDto.getPassword() != null && !memberUpdateDto.getPassword().isEmpty()) {
            encodedPassword = passwordEncoder.encode(memberUpdateDto.getPassword());
        }
        member.updateMember(memberUpdateDto, encodedPassword);
        memberRepository.save(member);
    }

    // 회원 정보 삭제 하면거 Refresh 토큰 제거
    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        try {
            // 1. RefreshToken 삭제
            refreshTokenRepository.deleteByMemberId(memberId);

            // 2. 자녀 정보 삭제
            childRepository.deleteByMember(member);

            // 3. 회원 삭제
            memberRepository.delete(member);
        } catch (Exception e) {
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}