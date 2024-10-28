package com.triple.backend.feedback.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.feedback.entity.Feedback;
import com.triple.backend.feedback.entity.FeedbackId;
import com.triple.backend.feedback.repository.FeedbackRepository;
import com.triple.backend.feedback.service.FeedbackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private static final String HATE_HASH_KEY = "hates";
    private static final String LIKE_HASH_KEY = "likes";

    private final HashOperations<String, String, Set<Long>> hashOperations;
    private final FeedbackRepository feedbackRepository;

    /**
     * 도서 좋아요
     */
    @Override
    public void insertLike(Long childId, Long bookId) {
        FeedbackId feedbackId = new FeedbackId(childId, bookId);
        boolean likeExists = feedbackRepository.findLikeStatusByFeedbackId(feedbackId);

        if (likeExists) {
            log.warn("insertLike: DB 정합성 불일치. 이미 좋아요 한 도서 입니다.");
            return;
        }

        Set<Long> hateBooks = hashOperations.get(HATE_HASH_KEY, String.valueOf(childId));

        if (hateBooks != null && hateBooks.contains(bookId)) {
            hateBooks.remove(bookId);
            if (hateBooks.isEmpty()) {
                hashOperations.delete(HATE_HASH_KEY, String.valueOf(childId));
            } else {
                hashOperations.put(HATE_HASH_KEY, String.valueOf(childId), hateBooks);
            }
        }

        boolean hateExists = feedbackRepository.findHateStatusByFeedbackId(feedbackId);

        if (hateExists) {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> NotFoundException.entityNotFound("피드백"));
            feedback.updateHateStatus(false);
        }

        Set<Long> likeStatus = hashOperations.get(LIKE_HASH_KEY, String.valueOf(childId));

        if (likeStatus == null) {
            likeStatus = new HashSet<>();
        }

        likeStatus.add(bookId);
        hashOperations.put(LIKE_HASH_KEY, String.valueOf(childId), likeStatus);
    }

    /**
     * 도서 좋아요 취소
     */
    @Override
    public void deleteLike(Long childId, Long bookId) {
        FeedbackId feedbackId = new FeedbackId(childId, bookId);
        Set<Long> likeBooks = hashOperations.get(LIKE_HASH_KEY, String.valueOf(childId));

        if (likeBooks == null) {
            log.warn("deleteLike: DB 정합성 불일치. 존재하지 않는 좋아요 입니다.");
            return;
        }

        if (likeBooks.contains(bookId)) {
            likeBooks.remove(bookId);

            if (likeBooks.isEmpty()) {
                hashOperations.delete(LIKE_HASH_KEY, String.valueOf(childId));
            } else {
                hashOperations.put(LIKE_HASH_KEY, String.valueOf(childId), likeBooks);
            }

            return;
        }

        boolean likeExists = feedbackRepository.findLikeStatusByFeedbackId(feedbackId);

        if (likeExists) {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> NotFoundException.entityNotFound("피드백"));
            feedback.updateLikeStatus(false);
        }
    }

    /**
     * 도서 싫어요
     */
    @Override
    public void insertHate(Long childId, Long bookId) {
        FeedbackId feedbackId = new FeedbackId(childId, bookId);
        boolean hateExists = feedbackRepository.findHateStatusByFeedbackId(feedbackId);

        if (hateExists) {
            log.warn("insertHate: DB 정합성 불일치. 이미 존재하는 싫어요입니다.");
            return;
        }

        Set<Long> likedBooks = hashOperations.get(LIKE_HASH_KEY, String.valueOf(childId));
        if (likedBooks != null && likedBooks.contains(bookId)) {
            likedBooks.remove(bookId);
            if (likedBooks.isEmpty()) {
                hashOperations.delete(LIKE_HASH_KEY, childId);
            }
            hashOperations.put(LIKE_HASH_KEY, String.valueOf(childId), likedBooks);
        } else {
            boolean likeExists = feedbackRepository.findLikeStatusByFeedbackId(feedbackId);
            if (likeExists) {
                Feedback feedback = feedbackRepository.findById(feedbackId)
                        .orElseThrow(() -> NotFoundException.entityNotFound("피드백"));
                feedback.updateLikeStatus(false);
            } else {
                Set<Long> hatedBooks = hashOperations.get(HATE_HASH_KEY, String.valueOf(childId));
                if (hatedBooks == null) {
                    hatedBooks = new HashSet<>();
                }

                hatedBooks.add(bookId);
                hashOperations.put(HATE_HASH_KEY, String.valueOf(childId), hatedBooks);
            }
        }
    }

    /**
     * 도서 싫어요 취소
     */
    @Override
    public void deleteHate(Long childId, Long bookId) {
        FeedbackId feedbackId = new FeedbackId(childId, bookId);
        Set<Long> hatedBooks = hashOperations.get(HATE_HASH_KEY, String.valueOf(childId));

        if (hatedBooks == null) {
            log.warn("deleteHate: DB 정합성 불일치. 존재하지 않는 싫어요입니다.");
            return;
        }

        if (hatedBooks.contains(bookId)) {
            hatedBooks.remove(bookId);
            if (hatedBooks.isEmpty()) {
                hashOperations.delete(HATE_HASH_KEY, String.valueOf(childId));
            } else {
                hashOperations.put(HATE_HASH_KEY, String.valueOf(childId), hatedBooks);
            }
            return;
        }

        boolean hateExists = feedbackRepository.findHateStatusByFeedbackId(feedbackId);
        if (hateExists) {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> NotFoundException.entityNotFound("피드백"));
            feedback.updateHateStatus(false);
        }
    }
}
