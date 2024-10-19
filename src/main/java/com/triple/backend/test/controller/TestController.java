package com.triple.backend.test.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    // 자녀 성향 질문 조회
    @GetMapping("/{testId}")
    public ResponseEntity<?> getTestQuestion(@PathVariable(name = "testId") Long testId,
                                             @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(CommonResponse.ok("Get TestQuestion Success", testService.getTestQuestion(testId, pageable)));
    }

}
