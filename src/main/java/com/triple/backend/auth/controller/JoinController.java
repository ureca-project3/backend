package com.triple.backend.auth.controller;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.auth.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public String joinProcess(JoinDto joinDto, RedirectAttributes redirectAttributes) {
        joinService.joinProcess(joinDto);

        // 회원가입 성공 메시지 추가 (필요에 따라 수정 가능)
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다!");

        // index.html로 리다이렉트
        return "redirect:/index.html";
    }
}
