package com.triple.backend.member.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.entity.MemberInfoDto;
import com.triple.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/mypage")
public class MyInfoController {

    private final MemberService memberService;

    @GetMapping("/my-info")
    public ResponseEntity<Map<String, Object>> getMemberInfo(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Member member = memberDetails.getMember();

        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("isKakaoUser", "kakao".equalsIgnoreCase(member.getProvider()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/my-info")
    public ResponseEntity<Map<String, String>> updateMemberInfo(
            @RequestBody Member member,
            Authentication authentication) {

        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Member currentMember = memberDetails.getMember();

        // 카카오 로그인 사용자 체크
        if ("kakao".equalsIgnoreCase(currentMember.getProvider())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "카카오 로그인 사용자는 정보를 수정할 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 이메일 중복 확인
        if (memberService.isEmailDuplicate(member.getEmail(), currentMember.getMemberId())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일이 이미 존재합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            memberService.updateMemberInfo(currentMember.getMemberId(), member);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Update MyInfo Success");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteMember(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Member member = memberDetails.getMember();
        try {
            memberService.deleteMember(member.getMemberId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Delete Member Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Delete Member Failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}