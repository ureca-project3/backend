package com.triple.backend.feedback.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/hates/{bookId}")
    public ResponseEntity<?> insertHate (@PathVariable(name = "bookId") Long bookId,
                                         @RequestHeader(name = "Child-Id") Long childId) {
        feedbackService.insertHate(childId, bookId);
        return CommonResponse.created("Insert Hate Success");
    }

    @DeleteMapping("/hates/{bookId}")
    public ResponseEntity<?> deleteHate (@PathVariable(name = "bookId") Long bookId,
                                         @RequestHeader(name = "Child-Id") Long childId) {
        feedbackService.deleteHate(childId, bookId);
        return CommonResponse.created("Delete Hate Success");
    }

}
