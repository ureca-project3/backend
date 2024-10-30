package com.triple.backend.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthPageController {

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/success")
    public String authSuccess(@RequestParam String accessToken, Model model) {
        // 토큰을 모델에 추가
        model.addAttribute("accessToken", accessToken);
        // auth-success.html 템플릿을 반환
        return "auth-success";
    }
}