package com.triple.backend.feedback.service;

public interface FeedbackService {

    // 도서 좋아요
    void insertLike(Long childId, Long bookId);

    void insertHate(Long childId, Long bookId);
    void deleteHate(Long childId, Long bookId);
}
