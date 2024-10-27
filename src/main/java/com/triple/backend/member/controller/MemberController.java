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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    @GetMapping("/user/profile")
    public ResponseEntity<MemberInfoDto> getUserProfile(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();

        // 회원 정보와 자녀 프로필을 함께 반환하는 서비스 호출
        MemberInfoDto userProfile = memberService.getUserProfileById(memberId);

        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/member/provider")
    public ResponseEntity<Map<String, String>> getProvider(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();

        // memberId를 통해 provider 정보 조회
        String provider = memberService.getProviderByMemberId(memberId);

        Map<String, String> response = new HashMap<>();
        response.put("provider", provider);
        return ResponseEntity.ok(response);
    }
}