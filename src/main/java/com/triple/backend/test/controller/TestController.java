package com.triple.backend.test.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.test.dto.TestAnswerRequestDto;
import com.triple.backend.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 자녀 성향 진단 결과 등록
    @PostMapping("/{testId}")
    public ResponseEntity<?> InsertTestResult(@PathVariable(name = "testId") Long testId,
                                              @RequestBody TestAnswerRequestDto testAnswerRequestDto) {
        testService.insertTestResult(testId, testAnswerRequestDto);
        return ResponseEntity.ok(CommonResponse.created("Insert TestResult Success"));
    }


}
