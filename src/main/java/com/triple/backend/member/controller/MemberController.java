package com.triple.backend.member.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.child.dto.ChildDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.entity.MemberInfoDto;
import com.triple.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 헤더에 사용자 access Token 담기
    @GetMapping("/user")
    public ResponseEntity<Member> getUser(@AuthenticationPrincipal CustomMemberDetails userDetails) { // @AuthenticationPrincipal: 컨트롤러 메서드에서 CustomMemberDetails 객체를 주입받음
        // CustomMemberDetails에서 Member 객체를 가져와 반환
        Member member = userDetails.getMember();
        return ResponseEntity.ok(member);
    }

    // 자녀 프로필 선택시 자녀 데이터 제공
    @GetMapping("/member/children")
    public ResponseEntity<List<ChildDto>> getChildren(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();

        List<ChildDto> children = memberService.getChildrenByMemberId(memberId);
        return ResponseEntity.ok(children);
    }
}