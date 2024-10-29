package com.triple.backend.member.service;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.entity.MemberInfoDto;
import com.triple.backend.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService, UserDetailsService {
    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 의존성

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
                member.getProvider(),
                children
        );
    }

    // memberId를 기반으로 provider 정보 조회 - 카카오 계정 로그아웃 시 사용
    public String getProviderByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return member.getProvider(); // provider 정보 반환
    }

    // 이메일 중복 확인 메서드
    @Override
    public boolean isEmailDuplicate(String email, Long memberId) {
        return memberRepository.existsByEmailAndMemberIdNot(email, memberId);
    }


    @Override
    public void updateMemberInfo(Long memberId, Member updatedMember) {
        // 기존 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 필드 업데이트
        member.setName(updatedMember.getName());
        member.setEmail(updatedMember.getEmail());
        member.setPhone(updatedMember.getPhone());

        // 비밀번호 암호화하여 업데이트 (비어 있지 않은 경우에만)
        if (updatedMember.getPassword() != null && !updatedMember.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updatedMember.getPassword());
            member.setPassword(encodedPassword);
        }

        // 변경된 정보 저장
        memberRepository.save(member);
    }

    @Override
    @Transactional // 데이터 일관성
    public void deleteMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("Member not found");
        }
        memberRepository.deleteById(memberId);
    }
}