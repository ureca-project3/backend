package com.triple.backend.child.controller;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class ChildController {

    private final MbtiHistoryService mbtiHistoryService;

    // 자녀 성향 히스토리 논리적 삭제
    @PatchMapping("/child-info/{historyId}")
    public ResponseEntity<?> deleteMyChildTraitHistory(@PathVariable Long historyId) {
        MbtiHistoryDeletedResponseDto mbtiHistoryDeletedResponseDto = mbtiHistoryService.deleteMyChildTraitHistory(historyId);
        return CommonResponse.ok("Delete MyChildHistory Success", mbtiHistoryDeletedResponseDto);
    }

    // 자녀 성향 히스토리 물리적 삭제 테스트용 컨트롤러
//    @DeleteMapping("/child-info/cleanup-old")
//    public ResponseEntity<?> cleanUpOldMbtiHistory() {
//        mbtiHistoryService.cleanUpOldRecords();
//        return CommonResponse.ok("Cleanup Old MbtiHistory Success");
//    }

}
