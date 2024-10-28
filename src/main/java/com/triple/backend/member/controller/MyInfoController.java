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


    // 사용자 정보 수정 엔드포인트
    @PatchMapping("/my-info")
    public ResponseEntity<Map<String, String>> updateMemberInfo(
            @RequestBody Member member,
            Authentication authentication) {

        // 인증된 사용자의 ID 가져오기
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();

        // 서비스에서 정보 업데이트 실행
        memberService.updateMemberInfo(memberId, member);

        // 응답 메시지 생성
        Map<String, String> response = new HashMap<>();
        response.put("message", "Update MyInfo Success");
        return ResponseEntity.ok(response);
    }

    // 회원 탈퇴 엔드포인트
    @DeleteMapping("/my-info")
    public ResponseEntity<Map<String, String>> deleteMember(Authentication authentication) {

        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMember().getMemberId();
        memberService.deleteMember(memberId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete Member Success");
        return ResponseEntity.ok(response);
    }

}
