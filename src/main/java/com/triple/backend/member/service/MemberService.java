package com.triple.backend.member.service;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.member.entity.Member;
import com.triple.backend.child.entity.Child;
import com.triple.backend.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service // 이 클래스가 서비스 컴포넌트임을 명시
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;

    public MemberService(MemberRepository memberRepository, ChildRepository childRepository){
        this.memberRepository = memberRepository;
        this.childRepository = childRepository;
    }


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

    // memberId로 이메일 조회
    public String getEmailByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getEmail)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    // memberId로 자녀 ID 목록 조회
    public List<Long> getChildIdsByMemberId(Long memberId) {
        // memberId로 Member 객체 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 memberId를 가진 회원을 찾을 수 없습니다."));

        // Member 객체로 자녀 목록 조회
        return childRepository.findAllByMember(member)
                .stream()
                .map(Child::getChildId)  // Child 엔티티에서 자녀의 ID를 가져옴
                .collect(Collectors.toList());
    }
}
