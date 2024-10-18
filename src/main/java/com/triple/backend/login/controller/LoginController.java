package com.triple.backend.login.controller;

import com.triple.backend.common.jwt.JWTUtil;
import com.triple.backend.login.dto.LoginDto;
import com.triple.backend.login.dto.LoginRequestDto;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class LoginController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginDto> login(
            @RequestBody LoginRequestDto loginRequest,
            @RequestParam(name = "name", required = false) String name) {

        // Member 인증
        Member member = memberService.authenticateByEmail(loginRequest.getEmail(), loginRequest.getPassword());

        if (member != null) {
            String accessToken = jwtUtil.generateToken(member.getEmail());
            String refreshToken = jwtUtil.generateToken(member.getEmail());

            // 응답 생성
            LoginDto.Data tokens = new LoginDto.Data(accessToken, refreshToken);
            LoginDto response = new LoginDto("Login Success", tokens, String.valueOf(System.currentTimeMillis()));

            return ResponseEntity.ok(response);
        }

        // 인증 실패 시 응답
        return ResponseEntity.status(401).body(new LoginDto("Unauthorized", null, String.valueOf(System.currentTimeMillis())));
    }
}
