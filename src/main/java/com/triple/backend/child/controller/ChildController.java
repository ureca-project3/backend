package com.triple.backend.child.controller;

import com.triple.backend.child.dto.*;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.triple.backend.child.service.ChildService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class ChildController {

    private final ChildService childService;
    private final MbtiHistoryService mbtiHistoryService;

    // 자녀 정보, 최신 히스토리 조회
    @GetMapping("/child-info/{childId}")
    public ResponseEntity<?> getChildInfo(@PathVariable(name = "childId") Long childId) {
        ChildInfoResponseDto childInfo = childService.getChildInfo(childId);
        return CommonResponse.ok("Get MyChildInfo Success", childInfo);
    }

    // 자녀 히스토리 조회
    @GetMapping("/child-info/history/{childId}")
    public ResponseEntity<?> getChildHistory(@PathVariable(name = "childId") Long childId, @RequestParam LocalDateTime date) {
        ChildHistoryResponseDto childHistory = childService.getChildHistory(childId, date);
        return CommonResponse.ok("Get MyChildHistory Success", childHistory);
    }

    // 자녀 결과 모음 조회
    @GetMapping("/child-info/result/{childId}")
    public ResponseEntity<?> getChildTestHistory(@PathVariable(name = "childId") Long childId) {
        ChildTestHistoryResponseDto childTestHistory = childService.getChildTestHistory(childId);
        return CommonResponse.ok("Get MyChildTestHistory Success", childTestHistory);
    }

    // 자녀 성향 진단 결과 모음 날짜 조회
    @GetMapping("/child-info/result/history/{childId}")
    public ResponseEntity<?> getChildTestHistoryDate(@PathVariable(name = "childId") Long childId, @RequestParam LocalDateTime date) {
        ChildTestHistoryDateResponseDto childTestHistory = childService.getChildTestHistoryDate(childId, date);
        return CommonResponse.ok("Get MyChildTestHistory Success", childTestHistory);
    }

    // 자녀 성향 히스토리 논리적 삭제
    @PatchMapping("/child-info/{historyId}")
    public ResponseEntity<?> deleteMyChildTraitHistory(@PathVariable Long historyId, @RequestHeader(name = "Child-Id") Long childId) {
        MbtiHistoryDeletedResponseDto mbtiHistoryDeletedResponseDto = mbtiHistoryService.deleteMyChildTraitHistory(historyId, childId);
        return CommonResponse.ok("Delete MyChildHistory Success", mbtiHistoryDeletedResponseDto);
    }

    // 자녀 등록
    @PostMapping("/child-info")
    public ResponseEntity<?> registerChild(
            @RequestBody ChildRegisterRequestDto request,
            @RequestHeader(name = "Authorization") String accessToken
    ) {
        childService.registerChild(request, accessToken.substring(7));
        ChildRegisterResponseDto response = new ChildRegisterResponseDto();
        response.setData(new ChildRegisterResponseDto.ChildData(request.getName()));
        return CommonResponse.ok("자녀등록을 성공했습니다.", response);
    }

    // 자녀 삭제
    @DeleteMapping("/child-child-info/{childId}")
    public ResponseEntity<?> deleteChildProfile(
            @PathVariable(name = "childId") Long childId,
            @RequestHeader(name = "Authorization") String accessToken
    ) {
        childService.deleteChildById(childId, accessToken.substring(7));
        return CommonResponse.ok("Delete Child Success", null);
    }
}