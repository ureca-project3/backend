package com.triple.backend.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html 파일을 반환합니다. (뷰 리졸버에 따라 경로 조정 필요)
    }

    @PostMapping("/login")
    public String loginProcess(String email, String password, RedirectAttributes redirectAttributes) {
        // 로그인 처리 로직 (예: 인증 및 권한 부여)
        // 여기서는 인증 로직이 필요합니다. 이 부분은 JWTFilter에서 처리하므로 생략 가능

        // 인증 성공 시
        redirectAttributes.addFlashAttribute("message", "로그인 성공!");
        return "redirect:/index.html"; // 로그인 성공 후 index.html로 리다이렉트
    }
}
