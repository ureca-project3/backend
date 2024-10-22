package com.triple.backend.auth.controller;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.auth.service.JoinService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Iterator;

@Controller
@ResponseBody
public class JoinController {

    private final JoinService joinService;

    // joinService을 이용하기 위한 생성자 초가화
    public JoinController(JoinService joinService){
        this.joinService = joinService;

    }

    @PostMapping("/join")
    public String joinProcess(JoinDto joinDto){
        joinService.joinProcess(joinDto);

        return "회원가입 완료";
    }

}