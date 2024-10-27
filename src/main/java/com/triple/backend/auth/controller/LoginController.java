package com.triple.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html 파일을 반환합니다. (뷰 리졸버에 따라 경로 조정 필요)
    }

    @GetMapping("/kakao-login-info")
    public Map<String, String> getKakaoLoginInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("clientId", clientId);
        response.put("redirectUri", redirectUri);
        return response;
    }
}