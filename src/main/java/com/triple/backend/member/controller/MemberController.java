package com.triple.backend.member.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.member.entity.MemberInfoDto;
import com.triple.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원 정보 조회 API
    @GetMapping("/info")
    public ResponseEntity<MemberInfoDto> getMemberInfo(Authentication authentication) {
        // 인증된 사용자의 정보를 Authentication 객체로부터 가져옴
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();

        // 회원 정보 조회
        MemberInfoDto memberInfo = memberService.getMemberInfoById(memberId);

        return ResponseEntity.ok(memberInfo);
    }
}