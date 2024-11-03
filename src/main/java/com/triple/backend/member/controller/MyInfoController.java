package com.triple.backend.member.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.member.dto.MemberUpdateDto;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.dto.MemberInfoDto;
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

    // 사용자 등록
    @GetMapping("/my-info")
    public ResponseEntity<Map<String, Object>> getMemberInfo(Authentication authentication) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Member member = memberDetails.getMember();

        // MemberInfoDto를 사용하여 자녀 정보를 포함한 응답 생성
        MemberInfoDto memberInfo = memberService.getUserProfileById(member.getMemberId());

        Map<String, Object> response = new HashMap<>();
        response.put("member", memberInfo);
        response.put("isKakaoUser", "kakao".equalsIgnoreCase(member.getProvider()));

        return ResponseEntity.ok(response);
    }

    // 사용자 개인정보 수정
    @PostMapping("/my-info")
    public ResponseEntity<Map<String, String>> updateMemberInfo(
            @RequestBody MemberUpdateDto member,
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

    // 사용자 삭제 
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