package com.triple.backend.feedback.repository;

import com.triple.backend.feedback.entity.Feedback;
import com.triple.backend.feedback.entity.FeedbackId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, FeedbackId> {

    // 좋아요 상태 확인
    @Query(value = "select case when count(f) > 0 then true else false end " +
            "from Feedback f where f.feedbackId = :feedbackId and f.likeStatus = true")
    boolean findLikeStatusByFeedbackId(@Param(value = "feedbackId") FeedbackId feedbackId);

    // 싫어요 상태 확인
    @Query(value = "select case when count(f) > 0 then true else false end " +
            "from Feedback f where f.feedbackId = :feedbackId and f.hateStatus = true")
    boolean findHateStatusByFeedbackId(@Param(value = "feedbackId") FeedbackId feedbackId);
}
