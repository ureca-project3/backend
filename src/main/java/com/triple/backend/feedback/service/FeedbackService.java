package com.triple.backend.feedback.service;

public interface FeedbackService {

    // 도서 좋아요
    void insertLike(Long childId, Long bookId);

    // 도서 좋아요 취소
    void deleteLike(Long childId, Long bookId);

    // 도서 싫어요
    void insertHate(Long childId, Long bookId);

    // 도서 싫어요 취소
    void deleteHate(Long childId, Long bookId);

    String getLikeStatus(Long childId, Long bookId);

    String getHateStatus(Long childId, Long bookId);
}
