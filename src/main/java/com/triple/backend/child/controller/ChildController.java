package com.triple.backend.child.controller;

import com.triple.backend.child.dto.ChildRegisterRequestDto;
import com.triple.backend.child.dto.ChildRegisterResponseDto;
import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.service.ChildService;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class ChildController {

    private final MbtiHistoryService mbtiHistoryService;
    private final ChildService childService;

    // 자녀 성향 히스토리 논리적 삭제
    @PatchMapping("/child-info/{historyId}")
    public ResponseEntity<?> deleteMyChildTraitHistory(@PathVariable Long historyId, @RequestHeader(name = "Child-Id") Long childId) {
        MbtiHistoryDeletedResponseDto mbtiHistoryDeletedResponseDto = mbtiHistoryService.deleteMyChildTraitHistory(historyId, childId);
        return CommonResponse.ok("Delete MyChildHistory Success", mbtiHistoryDeletedResponseDto);
    }

    // 자녀 성향 히스토리 물리적 삭제 테스트용 컨트롤러
//    @DeleteMapping("/child-info/cleanup-old")
//    public ResponseEntity<?> cleanUpOldMbtiHistory() {
//        mbtiHistoryService.cleanUpOldRecords();
//        return CommonResponse.ok("Cleanup Old MbtiHistory Success");
//    }



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

}
