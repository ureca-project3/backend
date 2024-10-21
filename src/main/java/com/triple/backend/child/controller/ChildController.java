package com.triple.backend.child.controller;

import com.triple.backend.child.dto.MbtiHistoryDeletedResponseDto;
import com.triple.backend.child.service.MbtiHistoryService;
import com.triple.backend.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
