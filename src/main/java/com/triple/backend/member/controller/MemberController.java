package com.triple.backend.member.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.child.dto.ChildDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.common.dto.CommonResponse;
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
    public ResponseEntity<CommonResponse<MemberInfoDto>> getUserProfile(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();
        MemberInfoDto userProfile = memberService.getUserProfileById(memberId);
        return CommonResponse.ok("Get User Profile Success", userProfile);
    }

    @GetMapping("/member/provider")
    public ResponseEntity<CommonResponse<Map<String, String>>> getProvider(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();
        String provider = memberService.getProviderByMemberId(memberId);
        Map<String, String> response = new HashMap<>();
        response.put("provider", provider);
        return CommonResponse.ok("Get Provider Success", response);
    }

    @GetMapping("/user")
    public ResponseEntity<CommonResponse<Member>> getUser(@AuthenticationPrincipal CustomMemberDetails userDetails) {
        Member member = userDetails.getMember();
        return CommonResponse.ok("Get User Success", member);
    }

    @GetMapping("/member/children")
    public ResponseEntity<CommonResponse<List<ChildDto>>> getChildren(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();
        List<ChildDto> children = memberService.getChildrenByMemberId(memberId);
        return CommonResponse.ok("Get Children Success", children);
    }
}