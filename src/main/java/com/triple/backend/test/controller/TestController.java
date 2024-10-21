package com.triple.backend.test.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.test.dto.TestResultDto;
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
        return CommonResponse.ok("Get TestQuestion Success", testService.getTestQuestion(testId, pageable));
    }

    /**
     *	자녀 성향 진단 결과 조회
     * 	childId는 헤더에 포함
     */
    @GetMapping("/result")
    public ResponseEntity<?> getTestResult(@RequestHeader(name = "Child-Id") Long childId) {
        TestResultDto testResultDto = testService.getTestResult(childId);
        return CommonResponse.ok("Get TestResult Success", testResultDto);
    }
}
