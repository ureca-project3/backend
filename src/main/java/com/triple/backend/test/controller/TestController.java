package com.triple.backend.test.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{testId}")
    public ResponseEntity<?> startTraitTest(@PathVariable(name = "testId") Long testId) {
        return ResponseEntity.ok(CommonResponse.ok("Start TraitTest Success", testService.getTestQuestion(testId)));
    }

}
