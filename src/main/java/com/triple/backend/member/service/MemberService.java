package com.triple.backend.member.service;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.member.entity.Member;
import com.triple.backend.child.entity.Child;
import com.triple.backend.member.entity.MemberInfoDto;
import com.triple.backend.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

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

//    // memberId로 이메일 조회
//    public String getEmailByMemberId(Long memberId) {
//        return memberRepository.findById(memberId)
//                .map(Member::getEmail)
//                .orElseThrow(() -> new RuntimeException("Member not found"));
//    }
//
//    // memberId로 자녀 ID 목록 조회
//    public List<Long> getChildIdsByMemberId(Long memberId) {
//        // memberId로 Member 객체 조회
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 memberId를 가진 회원을 찾을 수 없습니다."));
//
//        // Member 객체로 자녀 목록 조회
//        return childRepository.findAllByMember(member)
//                .stream()
//                .map(Child::getChildId)  // Child 엔티티에서 자녀의 ID를 가져옴
//                .collect(Collectors.toList());
//    }

//    // memberId를 기반으로 회원 정보 조회
//    public MemberInfoDto getMemberInfoById(Long memberId) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new RuntimeException("Member not found"));
//
//        // DTO로 변환하여 반환
//        return new MemberInfoDto(member.getName(), member.getEmail(), member.getPhone());
//    }
}
