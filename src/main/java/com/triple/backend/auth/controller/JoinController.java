package com.triple.backend.auth.controller;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.auth.dto.LoginRequest; // LoginRequest DTO 추가
import com.triple.backend.auth.service.JoinService;
import com.triple.backend.member.entity.Member;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 추가
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService 추가
import org.springframework.security.core.AuthenticationException; // AuthenticationException 추가

@Controller
public class JoinController {

    private final JoinService joinService;
    private final AuthenticationManager authenticationManager; // AuthenticationManager 추가

    // 생성자 주입
    public JoinController(JoinService joinService, AuthenticationManager authenticationManager) {
        this.joinService = joinService;
        this.authenticationManager = authenticationManager; // 초기화
    }

    @PostMapping("/join")
    public String joinProcess(JoinDto joinDto) {
        joinService.joinProcess(joinDto);
        return "회원가입 완료";
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
//        try {
//            // Spring Security의 AuthenticationManager를 사용하여 인증 시도
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
//            );
//
//            // 인증 성공 시 세션에 정보 저장
//            Member authenticatedUser = (Member) authentication.getPrincipal(); // UserDetails를 User로 캐스팅
//            session.setAttribute("email", authenticatedUser.getEmail()); // email은 UserDetails의 username으로 가져옴
//            session.setAttribute("userId", authenticatedUser.getMemberId()); // 사용자 ID를 저장
//
//            // 디버깅 로그 추가
//            System.out.println("Email: " + authenticatedUser.getEmail());
//            System.out.println("User ID: " + authenticatedUser.getMemberId());
//
//            return ResponseEntity.ok("로그인 성공");
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
//        }
//    }

}
