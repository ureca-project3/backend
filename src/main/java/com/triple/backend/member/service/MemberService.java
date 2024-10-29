package com.triple.backend.member.service;

import com.triple.backend.child.dto.ChildDto;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.entity.MemberInfoDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface MemberService {

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    // 특정 이메일로 사용자 조회
    Member findByEmail(String email);

    // 회원과 자녀 정보를 포함하는 UserProfileDto 조회
    MemberInfoDto getUserProfileById(Long memberId);

    // memberId를 기반으로 provider 정보 조회 - 카카오 계정 로그아웃 시 사용
    String getProviderByMemberId(Long memberId);

    // 자녀 프로필 선택시 자녀 데이터 제공
    List<ChildDto> getChildrenByMemberId(Long memberId);


}
