package com.triple.backend.child.controller;

import com.triple.backend.child.dto.ChildInfoResponseDto;
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

    @GetMapping("/child-info/{childId}")
    public ResponseEntity<?> getChildInfo(@PathVariable(name = "childId") Long childId) {
        ChildInfoResponseDto childInfo = childService.getChildInfo(childId);
        return CommonResponse.ok("Get MyChildInfo Success", childInfo);
    }
}