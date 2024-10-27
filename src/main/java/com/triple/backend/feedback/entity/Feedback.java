package com.triple.backend.feedback.entity;

import com.triple.backend.book.entity.Book;
import com.triple.backend.child.entity.Child;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Feedback {

    @EmbeddedId
    private FeedbackId feedbackId;

    @ManyToOne
    @MapsId("childId")
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private boolean likeStatus;

    private boolean hateStatus;

    private LocalDateTime createdAt;

    public void updateLikeStatus(boolean status) {
        this.likeStatus = status;
    }

    public void updateHateStatus(boolean status) {
        this.hateStatus = status;
    }
}
