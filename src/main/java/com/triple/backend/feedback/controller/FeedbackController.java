package com.triple.backend.feedback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.feedback.service.FeedbackService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 도서 좋아요
     */
    @PostMapping("/likes/{bookId}")
    public ResponseEntity<?> insertLike(
        @PathVariable(name = "bookId") Long bookId,
        @RequestHeader(name = "Child-Id") Long childId
    ) {
        feedbackService.insertLike(bookId, childId);

        return CommonResponse.ok("Insert Like Success");
    }

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
