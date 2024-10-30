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
    public ResponseEntity<ChildRegisterResponseDto> registerChild(@RequestBody ChildRegisterRequestDto request, HttpServletRequest httpRequest) {
        String accessToken = httpRequest.getHeader("Authorization").substring(7);
        childService.registerChild(request, accessToken);

        // 응답 생성
        ChildRegisterResponseDto response = new ChildRegisterResponseDto();
        response.setMessage("Insert MyChild Success");
        response.setData(new ChildRegisterResponseDto.ChildData(request.getName())); // request.getName() 사용
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 자녀 삭제
    @DeleteMapping("/child-info")
    public ResponseEntity<String> deleteChildProfile(HttpServletRequest request) {
        // 액세스 토큰에서 사용자 ID(memberId) 추출
        String accessToken = request.getHeader("Authorization").substring(7);
        boolean isDeleted = childService.deleteMyChild(accessToken);

        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("자녀 프로필 정보가 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("자녀 프로필 정보를 찾을 수 없습니다.");
        }
    }
}