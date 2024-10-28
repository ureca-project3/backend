package com.triple.backend.mbti.controller;

import com.triple.backend.mbti.scheduler.MbtiScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MbtiController {

    private final MbtiScheduler mbtiScheduler;

    /*
    insertNewChildTraits() 테스트용 컨트롤러
     */
    @PostMapping("/mbti-test")
    public ResponseEntity<?> insertNewChildTraits() {
        mbtiScheduler.insertNewChildTraits();
        return ResponseEntity.ok().build();
    }
}
