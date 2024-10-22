package com.triple.backend.feedback.service;

public interface FeedbackService {
    void insertHate(Long childId, Long bookId);
    void deleteHate(Long childId, Long bookId);
}
