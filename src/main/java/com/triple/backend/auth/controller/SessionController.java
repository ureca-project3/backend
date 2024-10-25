package com.triple.backend.auth.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    @GetMapping("/session-info")
    public String getSessionInfo(HttpSession session) {
        String sessionId = session.getId();
        String email = (String) session.getAttribute("email");
        String userId = (String) session.getAttribute("userId");
        // 다른 속성도 가져올 수 있음

        return String.format("Session ID: %s, Email: %s, User ID: %s", sessionId, email, userId);
    }

}
