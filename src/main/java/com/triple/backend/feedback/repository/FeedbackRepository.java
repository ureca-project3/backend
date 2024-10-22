package com.triple.backend.feedback.repository;

import com.triple.backend.feedback.entity.Feedback;
import com.triple.backend.feedback.entity.FeedbackId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, FeedbackId> {
    @Query(value = "select f from Feedback f where f.feedbackId = :feedbackId and f.hateStatus = true")
    Optional<Feedback> findByIdAndHateStatusIsTrue(@Param("feedbackId") FeedbackId feedbackId);

    @Query(value = "select f from Feedback f where f.feedbackId = :feedbackId and f.likeStatus = true")
    Optional<Object> findByIdAndLikeStatusIsTrue(FeedbackId feedbackId);
}
