package com.triple.backend.child.controller;

import com.triple.backend.child.dto.ChildHistoryResponseDto;
import com.triple.backend.child.dto.ChildInfoResponseDto;
import com.triple.backend.child.dto.ChildTestHistoryResponseDto;
import com.triple.backend.child.service.ChildService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class ChildController {

    private final ChildService childService;

    // 자녀 정보, 최신 히스토리 조회
    @GetMapping("/child-info/{childId}")
    public ResponseEntity<?> getChildInfo(@PathVariable(name = "childId") Long childId) {
        ChildInfoResponseDto childInfo = childService.getChildInfo(childId);
        return CommonResponse.ok("Get MyChildInfo Success", childInfo);
    }

    // 자녀 히스토리 조회
    @GetMapping("/child-info/history/{childId}")
    public ResponseEntity<?> getChildHistory(@PathVariable(name = "childId") Long childId, @RequestParam String date) {
        ChildHistoryResponseDto childHistory = childService.getChildHistory(childId, date);
        return CommonResponse.ok("Get MyChildHistory Success", childHistory);
    }

    // 자녀 결과 조회
    @GetMapping("/child-info/result/{childId}")
    public ResponseEntity<?> getChildTestHistory(@PathVariable(name = "childId") Long childId) {
        ChildTestHistoryResponseDto childTestHistory = childService.getChildTestHistory(childId);
        return CommonResponse.ok("Get MyChildTestHistory Success", childTestHistory);
    }
}