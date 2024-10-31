package com.triple.backend.feedback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        feedbackService.insertLike(childId, bookId);

        return CommonResponse.created("Insert Like Success");
    }

    /**
     * 도서 좋아요 취소
     */
    @DeleteMapping("/likes/{bookId}")
    public ResponseEntity<?> deleteLike(
        @PathVariable(name = "bookId") Long bookId,
        @RequestHeader(name = "Child-Id") Long childId
    ) {
        feedbackService.deleteLike(childId, bookId);

        return CommonResponse.ok("Delete Like Success");
    }

    /**
     * 도서 싫어요
     */
    @PostMapping("/hates/{bookId}")
    public ResponseEntity<?> insertHate (@PathVariable(name = "bookId") Long bookId,
                                         @RequestHeader(name = "Child-Id") Long childId) {
        feedbackService.insertHate(childId, bookId);
        return CommonResponse.created("Insert Hate Success");
    }

    /**
     * 도서 싫어요 취소
     */
    @DeleteMapping("/hates/{bookId}")
    public ResponseEntity<?> deleteHate (@PathVariable(name = "bookId") Long bookId,
                                         @RequestHeader(name = "Child-Id") Long childId) {
        feedbackService.deleteHate(childId, bookId);
        return CommonResponse.ok("Delete Hate Success");
    }

    /**
     * 도서 좋아요 상태 확인
     */
    @GetMapping("/likes/{bookId}")
    public ResponseEntity<?> getLikeStatus(@PathVariable(name = "bookId") Long bookId,
                                           @RequestHeader(name = "Child-Id") Long childId) {
        String status = feedbackService.getLikeStatus(childId, bookId);
        return CommonResponse.ok(status);
    }

    /**
     * 도서 싫어요 상태 확인
     */
    @GetMapping("/hates/{bookId}")
    public ResponseEntity<?> getHateStatus(@PathVariable(name = "bookId") Long bookId,
                                           @RequestHeader(name = "Child-Id") Long childId) {
        String status = feedbackService.getHateStatus(childId, bookId);
        return CommonResponse.ok(status);
    }
}
